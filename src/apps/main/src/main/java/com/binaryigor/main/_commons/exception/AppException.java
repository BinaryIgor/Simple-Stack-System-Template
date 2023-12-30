package com.binaryigor.main._commons.exception;

import java.util.List;

public class AppException extends RuntimeException {

    static final String EXCEPTION_SUFFIX = "Exception";

    public AppException(String message) {
        super(message);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof AppException e) {
            return getClass().equals(e.getClass())
                    && getMessage().equals(e.getMessage());
        }

        return false;
    }

    public List<String> toErrors() {
        return defaultErrors(getClass());
    }

    public static List<String> defaultErrors(Class<? extends Throwable> exception) {
        return List.of(exception.getSimpleName().replace(EXCEPTION_SUFFIX, ""));
    }
}
