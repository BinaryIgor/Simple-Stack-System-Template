package com.binaryigor.main.user.common.exception;

import com.binaryigor.main._commons.core.exception.AppException;

public class InvalidEmailException extends AppException {

    public InvalidEmailException(String email) {
        super("%s is not a valid email".formatted(email));
    }
}
