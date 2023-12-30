package com.binaryigor.main._commons.exception;

public class UnauthenticatedException extends AppException {

    public UnauthenticatedException() {
        super("Authentication required");
    }
}
