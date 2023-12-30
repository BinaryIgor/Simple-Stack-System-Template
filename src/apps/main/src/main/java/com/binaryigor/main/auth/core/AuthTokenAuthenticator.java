package com.binaryigor.main.auth.core;

public interface AuthTokenAuthenticator {
    AuthenticationResult authenticate(String token);

    AuthToken refresh(String token);

}
