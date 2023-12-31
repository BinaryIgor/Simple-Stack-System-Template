package com.binaryigor.main.user.common.exception;

import com.binaryigor.main._commons.core.exception.ConflictException;

public class EmailTakenException extends ConflictException {

    public EmailTakenException(String email) {
        super("%s email is taken".formatted(email));
    }
}
