package com.binaryigor.main.user.auth.core.exception;

import com.binaryigor.main._commons.core.exception.AppException;

public class SecondFactorAuthenticationException extends AppException {

    public SecondFactorAuthenticationException(String message) {
        super(message);
    }
}
