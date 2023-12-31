package com.binaryigor.main.user.common.core.model;

import com.binaryigor.main._common.core.AppLanguage;

import java.util.UUID;

public record EmailUser(UUID id,
                        String name,
                        String email,
                        AppLanguage language) {

    public static EmailUser fromUser(User user) {
        return fromUser(user, user.email());
    }

    public static EmailUser fromUser(User user, String newEmail) {
        return new EmailUser(user.id(), user.name(), newEmail, user.language());
    }
}
