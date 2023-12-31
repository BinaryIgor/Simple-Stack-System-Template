package com.binaryigor.main._commons.core.exception;

public class InvalidAuthTokenException extends AppException {

    public InvalidAuthTokenException(String message) {
        super(message);
    }

    public static InvalidAuthTokenException invalidAccessToken() {
        return new InvalidAuthTokenException("Invalid access token");
    }

    public static InvalidAuthTokenException invalidRefreshToken() {
        return new InvalidAuthTokenException("Invalid refresh token");
    }

    public static InvalidAuthTokenException expiredToken() {
        return new InvalidAuthTokenException("Token has expired");
    }
}
