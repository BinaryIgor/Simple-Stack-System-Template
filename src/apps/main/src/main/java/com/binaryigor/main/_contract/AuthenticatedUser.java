package com.binaryigor.main._contract;

import java.util.Set;
import java.util.UUID;

public record AuthenticatedUser(UUID id,
                                UserState state,
                                boolean banned,
                                Set<UserRole> roles) {

    public static AuthenticatedUser withoutRoles(UUID id, UserState state, boolean banned) {
        return new AuthenticatedUser(id, state, banned, Set.of());
    }

    public boolean isAdmin() {
        return roles.contains(UserRole.ADMIN);
    }
}
