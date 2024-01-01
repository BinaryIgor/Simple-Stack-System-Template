package com.binaryigor.main.user.common.core.exception;

import com.binaryigor.main._common.core.exception.AppException;

public class InvalidPasswordException extends AppException {

    public InvalidPasswordException() {
        super("Password is not valid");
    }
}
