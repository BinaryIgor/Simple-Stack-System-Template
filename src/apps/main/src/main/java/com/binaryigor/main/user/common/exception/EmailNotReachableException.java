package com.binaryigor.main.user.common.exception;

import com.binaryigor.main._commons.core.exception.AppException;

public class EmailNotReachableException extends AppException {

    public EmailNotReachableException(String email) {
        super("%s email is not reachable".formatted(email));
    }
}
