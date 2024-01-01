package com.binaryigor.main.user.common.core.exception;

import com.binaryigor.main._common.core.exception.AppException;
import com.binaryigor.main.user.common.core.model.ActivationTokenId;

public class InvalidActivationTokenException extends AppException {

    public InvalidActivationTokenException(String message) {
        super(message);
    }

    public static InvalidActivationTokenException ofToken(String token) {
        return new InvalidActivationTokenException("%s is not a valid activation token".formatted(token));
    }

    public static InvalidActivationTokenException ofToken(ActivationTokenId id, String message) {
        return new InvalidActivationTokenException("%s: %s".formatted(id, message));
    }
}
