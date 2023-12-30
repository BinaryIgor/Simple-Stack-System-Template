package com.binaryigor.main._contract;

public enum UserState {
    CREATED, ACTIVATED;

    public boolean isAtLeast(UserState state) {
        return ordinal() >= state.ordinal();
    }
}