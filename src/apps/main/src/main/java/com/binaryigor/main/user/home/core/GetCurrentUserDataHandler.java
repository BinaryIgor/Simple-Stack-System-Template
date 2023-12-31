package com.binaryigor.main.user.home.core;

import com.binaryigor.main.user.auth.core.model.CurrentUserData;
import com.binaryigor.main.user.common.core.UserExceptions;
import com.binaryigor.main.user.common.core.repository.UserRepository;

import java.util.UUID;

public class GetCurrentUserDataHandler {

    private final UserRepository userRepository;

    public GetCurrentUserDataHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public CurrentUserData handle(UUID id) {
        var user = userRepository.ofId(id)
                .orElseThrow(() -> UserExceptions.userOfIdNotFound(id));
        return CurrentUserData.fromUser(user);
    }
}
