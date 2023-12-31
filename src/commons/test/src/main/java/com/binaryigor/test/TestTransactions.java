package com.binaryigor.test;

import com.binaryigor.types.Transactions;
import org.junit.jupiter.api.Assertions;

import java.util.function.Supplier;

public class TestTransactions implements Transactions {

    private Runnable afterCheck = () -> {
    };

    @Override
    public void execute(Runnable transaction) {
        executeAndReturn(() -> {
            transaction.run();
            return true;
        });
    }

    @Override
    public <T> T executeAndReturn(Supplier<T> transaction) {
        try {
            return transaction.get();
        } finally {
            afterCheck.run();
        }
    }

    public Test test() {
        return new Test();
    }

    public class Test {

        private Runnable before = () -> {
        };
        private boolean afterCheckCalled = false;

        public Test before(Runnable beforeCheck) {
            before = beforeCheck;
            return this;
        }

        public Test after(Runnable afterCheck) {
            TestTransactions.this.afterCheck = () -> {
                afterCheckCalled = true;
                afterCheck.run();
            };
            afterCheckCalled = false;
            return this;
        }

        public void execute(Runnable transaction) {
            before.run();
            try {
                transaction.run();
            } finally {
                Assertions.assertTrue(afterCheckCalled);
            }
        }
    }
}
