package com.binaryigor.main.user.common.core.exception;

import com.binaryigor.main._common.core.exception.AppException;

public class InvalidEmailException extends AppException {

    public InvalidEmailException(String email) {
        super("%s is not a valid email".formatted(email));
    }
}
