package com.binaryigor.main._contract;

import com.binaryigor.main._contract.model.UserAuthData;

import java.util.Optional;
import java.util.UUID;

public interface UserAuthClient {
    Optional<UserAuthData> dataOfId(UUID id);
}
