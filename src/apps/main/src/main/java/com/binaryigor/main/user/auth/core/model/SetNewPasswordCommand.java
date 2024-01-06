package com.binaryigor.main.user.auth.core.model;

public record SetNewPasswordCommand(String password,
                                    String repeatedPassword,
                                    String token) {
}
