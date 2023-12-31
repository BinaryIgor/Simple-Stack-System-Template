package com.binaryigor.main.user.common.exception;

import com.binaryigor.main._commons.core.exception.AppException;

public class NotMatchedPasswordException extends AppException {

    public NotMatchedPasswordException() {
        super("Password is not matching");
    }
}
