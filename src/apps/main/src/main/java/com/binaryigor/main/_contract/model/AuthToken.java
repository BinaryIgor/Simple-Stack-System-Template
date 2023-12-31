package com.binaryigor.main._contract.model;

import java.time.Instant;

public record AuthToken(String value, Instant expiresAt) {
}
