package com.binaryigor.main._contract;

import java.util.Set;
import java.util.UUID;

public record UserAuthData(UUID id,
                           UserState state,
                           boolean banned,
                           Set<UserRole> roles) {

    public AuthenticatedUser toAuthenticatedUser() {
        return new AuthenticatedUser(id, state, banned, roles);
    }
}
