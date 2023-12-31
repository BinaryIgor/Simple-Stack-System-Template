package com.binaryigor.main.user.common.core.exception;

import com.binaryigor.main._common.core.exception.AccessForbiddenException;

public class UserNotActivatedException extends AccessForbiddenException {

    public UserNotActivatedException() {
        super("User didn't activate their account");
    }
}
