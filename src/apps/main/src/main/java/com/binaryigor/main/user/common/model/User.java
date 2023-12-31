package com.binaryigor.main.user.common.model;

import com.binaryigor.main._commons.core.AppLanguage;
import com.binaryigor.main._contract.model.UserState;

import java.util.UUID;

public record User(UUID id,
                   String name,
                   String email,
                   AppLanguage language,
                   UserState state,
                   String password,
                   boolean secondFactorAuth) {

    public static User newUser(UUID id,
                               String name,
                               String email,
                               AppLanguage language,
                               String password) {
        return new User(id, name, email, language, UserState.CREATED, password, false);
    }
}
