package com.binaryigor.main.auth.core;

import java.util.UUID;

public interface AuthTokenCreator {

    AuthToken ofUser(UUID id);
}
