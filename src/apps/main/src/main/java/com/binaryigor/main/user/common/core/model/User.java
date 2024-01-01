package com.binaryigor.main.user.common.core.model;

import com.binaryigor.main._common.core.AppLanguage;
import com.binaryigor.main._contract.model.UserRole;
import com.binaryigor.main._contract.model.UserState;

import java.util.Set;
import java.util.UUID;

public record User(UUID id,
                   String name,
                   String email,
                   AppLanguage language,
                   UserState state,
                   Set<UserRole> roles,
                   String password,
                   boolean secondFactorAuth) {

    public static User newUser(UUID id,
                               String name,
                               String email,
                               AppLanguage language,
                               String password) {
        return new User(id, name, email, language, UserState.CREATED, Set.of(), password, false);
    }
}
