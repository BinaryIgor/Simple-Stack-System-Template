package com.binaryigor.main.user.auth.core.model;

public record SignInSecondStepCommand(String email, String code) {
}
