package com.binaryigor.main.user;

import com.binaryigor.main._contract.UserAuthClient;
import com.binaryigor.main._contract.UserAuthData;
import com.binaryigor.main._contract.UserClient;

import java.util.Optional;
import java.util.UUID;

public class DefaultUserClient implements UserAuthClient, UserClient {

    //TODO: impl!
    @Override
    public Optional<UserAuthData> dataOfId(UUID id) {
        return Optional.empty();
    }
}
