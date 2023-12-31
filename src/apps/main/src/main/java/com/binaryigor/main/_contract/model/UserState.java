package com.binaryigor.main._contract.model;

public enum UserState {
    CREATED, ACTIVATED;

    public boolean isAtLeast(UserState state) {
        return ordinal() >= state.ordinal();
    }
}