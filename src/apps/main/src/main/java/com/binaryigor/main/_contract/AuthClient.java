package com.binaryigor.main._contract;

import com.binaryigor.main._contract.model.AuthToken;

import java.util.UUID;

public interface AuthClient {

    AuthToken ofUser(UUID id);
}
