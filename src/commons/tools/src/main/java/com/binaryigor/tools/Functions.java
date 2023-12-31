package com.binaryigor.tools;

import java.util.concurrent.Callable;

public class Functions {

    public static void executeThrowable(ThrowableRunnable runnable) {
        try {
            runnable.run();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T callThrowable(Callable<T> callable) {
        try {
            return callable.call();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public interface ThrowableRunnable {
        void run() throws Throwable;
    }
}
