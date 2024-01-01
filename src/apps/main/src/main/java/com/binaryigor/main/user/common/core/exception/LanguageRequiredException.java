package com.binaryigor.main.user.common.core.exception;

import com.binaryigor.main._common.core.exception.AppException;

public class LanguageRequiredException extends AppException {

    public LanguageRequiredException() {
        super("Language is required, but was null");
    }
}
