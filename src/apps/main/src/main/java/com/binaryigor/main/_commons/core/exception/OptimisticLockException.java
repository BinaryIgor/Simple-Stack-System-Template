package com.binaryigor.main._commons.core.exception;

public class OptimisticLockException extends AppException {

    public OptimisticLockException(String entity) {
        super("Trying to save outdated %s entity".formatted(entity));
    }
}
