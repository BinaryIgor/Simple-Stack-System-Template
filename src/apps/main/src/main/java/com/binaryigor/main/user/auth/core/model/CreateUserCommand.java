package com.binaryigor.main.user.auth.core.model;

import com.binaryigor.main._commons.core.AppLanguage;

import java.util.UUID;

public record CreateUserCommand(UUID id,
                                String name,
                                String email,
                                AppLanguage language,
                                String password) {

    public CreateUserCommand(String name, String email, AppLanguage language, String password) {
        this(UUID.randomUUID(), name, email, language, password);
    }
}
