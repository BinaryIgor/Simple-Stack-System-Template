package com.binaryigor.main._common.core.exception;

public class InvalidNameException extends AppException {

    public InvalidNameException(String name) {
        super("%s is not a valid name".formatted(name));
    }
}
