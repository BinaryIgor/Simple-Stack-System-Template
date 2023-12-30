package com.binaryigor.main.auth.core;

import java.time.Instant;

public record AuthToken(String value, Instant expiresAt) {
}
