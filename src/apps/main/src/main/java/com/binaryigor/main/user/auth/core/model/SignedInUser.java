package com.binaryigor.main.user.auth.core.model;

import com.binaryigor.main._contract.model.AuthToken;

public record SignedInUser(CurrentUserData data,
                           AuthToken token) {
}
