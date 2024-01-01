package com.binaryigor.main.user.common.core.exception;

import com.binaryigor.main._common.core.exception.AppException;

public class NotMatchedPasswordException extends AppException {

    public NotMatchedPasswordException() {
        super("Password is not matching");
    }
}
