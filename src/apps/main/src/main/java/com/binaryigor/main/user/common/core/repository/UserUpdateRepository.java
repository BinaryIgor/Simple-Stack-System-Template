package com.binaryigor.main.user.common.core.repository;

import com.binaryigor.main._contract.model.UserState;

import java.util.UUID;

public interface UserUpdateRepository {

    void updateName(UUID id, String name);

    void updateEmail(UUID id, String email);

    void updateState(UUID id, UserState state);

    void updatePassword(UUID id, String password);
}
