package com.binaryigor.main._contract;

import com.binaryigor.main._contract.model.AuthenticatedUser;

import java.util.UUID;

public interface AuthUserClient {

    AuthenticatedUser current();

    UUID currentId();
}
