package com.binaryigor.main.user.common.core;

public interface PasswordHasher {

    String hash(String password);

    boolean matches(String rawPassword, String hashedPassword);
}
