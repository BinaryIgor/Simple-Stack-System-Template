package com.binaryigor.main.user.common.model;

import java.util.UUID;

public record ActivationTokenId(UUID userId, ActivationTokenType tokenType) {

    public static ActivationTokenId ofEmailChange(UUID userId) {
        return new ActivationTokenId(userId, ActivationTokenType.EMAIL_CHANGE);
    }

    public static ActivationTokenId ofNewUser(UUID userId) {
        return new ActivationTokenId(userId, ActivationTokenType.NEW_USER);
    }

    public static ActivationTokenId ofPasswordReset(UUID userId) {
        return new ActivationTokenId(userId, ActivationTokenType.PASSWORD_RESET);
    }
}
