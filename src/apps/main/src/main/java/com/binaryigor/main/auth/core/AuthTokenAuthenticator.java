package com.binaryigor.main.auth.core;

import com.binaryigor.main._contract.model.AuthToken;

public interface AuthTokenAuthenticator {
    AuthenticationResult authenticate(String token);

    AuthToken refresh(String token);

}
