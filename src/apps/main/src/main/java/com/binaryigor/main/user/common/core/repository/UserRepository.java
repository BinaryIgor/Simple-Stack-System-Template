package com.binaryigor.main.user.common.core.repository;

import com.binaryigor.main.user.common.core.model.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    UUID create(User user);

    Optional<User> ofEmail(String email);

    Optional<User> ofId(UUID id);
}
