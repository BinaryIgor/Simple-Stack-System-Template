package com.binaryigor.main.user.auth.core.model;

import com.binaryigor.main._commons.core.AppLanguage;
import com.binaryigor.main._contract.model.UserRole;
import com.binaryigor.main._contract.model.UserState;
import com.binaryigor.main.user.common.model.User;

import java.util.Collection;
import java.util.UUID;

public record CurrentUserData(UUID id,
                              String name,
                              String email,
                              AppLanguage language,
                              UserState state,
                              Collection<UserRole> roles) {

    public static CurrentUserData fromUser(User user,
                                           Collection<UserRole> roles) {
        return new CurrentUserData(user.id(), user.name(), user.email(),
                user.language(), user.state(), roles);
    }
}
