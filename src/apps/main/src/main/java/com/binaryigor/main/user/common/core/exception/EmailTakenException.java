package com.binaryigor.main.user.common.core.exception;

import com.binaryigor.main._commons.core.exception.ConflictException;

public class EmailTakenException extends ConflictException {

    public EmailTakenException(String email) {
        super("%s email is taken".formatted(email));
    }
}
