package com.binaryigor.main._common.core.exception;

public class UnauthenticatedException extends AppException {

    public UnauthenticatedException() {
        super("Authentication required");
    }
}
