package com.binaryigor.main.user.common.core;

public class DumbPasswordHasher implements PasswordHasher {

    @Override
    public String hash(String password) {
        return password;
    }

    @Override
    public boolean matches(String rawPassword, String hashedPassword) {
        return rawPassword.equals(hashedPassword);
    }
}
