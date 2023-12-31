package com.binaryigor.main.user.common.infra;

import com.binaryigor.main._contract.UserAuthClient;
import com.binaryigor.main._contract.UserClient;
import com.binaryigor.main._contract.model.UserAuthData;
import com.binaryigor.main.user.common.core.repository.UserRepository;

import java.util.Optional;
import java.util.UUID;

public class SqlUserClient implements UserAuthClient, UserClient {

    private final UserRepository userRepository;

    public SqlUserClient(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    //TODO: impl, TMP workaround!
    @Override
    public Optional<UserAuthData> dataOfId(UUID id) {
        return userRepository.ofId(id)
                .map(u -> new UserAuthData(id, u.state(), false, u.roles()));
    }
}
