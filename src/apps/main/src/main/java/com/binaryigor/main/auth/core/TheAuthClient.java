package com.binaryigor.main.auth.core;

import com.binaryigor.main._commons.core.exception.UnauthenticatedException;
import com.binaryigor.main._contract.AuthClient;
import com.binaryigor.main._contract.AuthUserClient;
import com.binaryigor.main._contract.model.AuthToken;
import com.binaryigor.main._contract.model.AuthenticatedUser;
import com.binaryigor.main.auth.app.AuthenticatedUserRequestHolder;

import java.util.UUID;

public class TheAuthClient implements AuthClient, AuthUserClient {

    private final AuthTokenCreator authTokenCreator;

    public TheAuthClient(AuthTokenCreator authTokenCreator) {
        this.authTokenCreator = authTokenCreator;
    }

    @Override
    public AuthToken ofUser(UUID id) {
        return authTokenCreator.ofUser(id);
    }

    @Override
    public AuthenticatedUser current() {
        return AuthenticatedUserRequestHolder.get()
                .orElseThrow(UnauthenticatedException::new);
    }

    @Override
    public UUID currentId() {
        return current().id();
    }
}
