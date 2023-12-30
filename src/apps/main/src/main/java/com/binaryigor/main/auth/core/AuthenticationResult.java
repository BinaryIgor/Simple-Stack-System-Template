package com.binaryigor.main.auth.core;

import com.binaryigor.main._contract.AuthenticatedUser;

import java.time.Instant;

public record AuthenticationResult(AuthenticatedUser user,
                                   Instant expiresAt) {
}
