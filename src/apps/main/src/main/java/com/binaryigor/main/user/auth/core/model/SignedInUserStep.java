package com.binaryigor.main.user.auth.core.model;

public record SignedInUserStep(boolean secondFactor, SignedInUser user) {

    public static SignedInUserStep firstStep() {
        return new SignedInUserStep(true, null);
    }

    public static SignedInUserStep onlyStep(SignedInUser user) {
        return new SignedInUserStep(false, user);
    }
}
