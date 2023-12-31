package com.binaryigor.main.auth.core;

import com.binaryigor.main._contract.model.AuthenticatedUser;

import java.time.Instant;

public record AuthenticationResult(AuthenticatedUser user,
                                   Instant expiresAt) {
}
