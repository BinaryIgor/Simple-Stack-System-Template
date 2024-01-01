package com.binaryigor.email.model;

public record EmailAddress(String name, String email) {

    public static EmailAddress ofNameEmail(String name, String email) {
        return new EmailAddress(name, email);
    }

    public static EmailAddress ofEmptyName(String email) {
        return new EmailAddress(null, email);
    }
}
