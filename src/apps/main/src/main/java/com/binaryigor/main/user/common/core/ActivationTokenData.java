package com.binaryigor.main.user.common.core;

import com.binaryigor.main.user.common.core.exception.InvalidActivationTokenException;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public record ActivationTokenData(Map<String, String> data) {
    public static final String USER_ID = "userId";
    public static final String NEW_EMAIL = "newEmail";

    public static ActivationTokenData withUserId(UUID id) {
        return new ActivationTokenData(Map.of(USER_ID, id.toString()));
    }

    public static ActivationTokenData withUserIdAndNewEmail(UUID id, String newEmail) {
        return new ActivationTokenData(Map.of(USER_ID, id.toString(), NEW_EMAIL, newEmail));
    }

    public UUID userId() {
        var value = valueOrThrow(USER_ID);
        try {
            return UUID.fromString(value);
        } catch (Exception e) {
            throw new InvalidActivationTokenException("Invalid user id format");
        }
    }

    public String newEmail() {
        return valueOrThrow(NEW_EMAIL);
    }

    public String valueOrThrow(String key) {
        return Optional.ofNullable(data.get(key))
                .orElseThrow(() -> new InvalidActivationTokenException("Lacking %s key in token data".formatted(key)));
    }
}
