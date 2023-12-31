package com.binaryigor.main.user.auth.core.model;

import java.time.Instant;
import java.util.UUID;

public record SecondFactorAuthentication(UUID userId,
                                         String email,
                                         String code,
                                         Instant sentAt,
                                         Instant expiresAt) {
}
