package com.binaryigor.main.auth.core;

import com.binaryigor.main._contract.model.AuthToken;

import java.util.UUID;

public interface AuthTokenCreator {

    AuthToken ofUser(UUID id);
}
