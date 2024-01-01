package com.binaryigor.main.user.auth.core.model;

import com.binaryigor.main._common.core.AppLanguage;

import java.util.UUID;

public record SignUpCommand(UUID id,
                            String name,
                            String email,
                            AppLanguage language,
                            String password) {

    public SignUpCommand(String name, String email, AppLanguage language, String password) {
        this(UUID.randomUUID(), name, email, language, password);
    }
}
