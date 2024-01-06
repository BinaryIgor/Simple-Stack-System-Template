package com.binaryigor.main.user.common.core.exception;

import com.binaryigor.main._common.core.exception.AppException;

public class DifferentPasswordException extends AppException {

    public DifferentPasswordException() {
        super("Repeated password is different");
    }
}
