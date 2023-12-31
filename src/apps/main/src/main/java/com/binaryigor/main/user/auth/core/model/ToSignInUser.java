package com.binaryigor.main.user.auth.core.model;

import com.binaryigor.main._contract.model.UserRole;
import com.binaryigor.main._contract.model.UserState;

import java.util.Set;
import java.util.UUID;

public record ToSignInUser(UUID id,
                           String name,
                           String email,
                           UserState state,
                           String password,
                           boolean secondFactorAuthentication,
                           Set<UserRole> roles) {
}
