package com.binaryigor.main.user.common.infra;

import com.binaryigor.main._contract.model.UserState;
import com.binaryigor.main.user.common.core.model.User;
import com.binaryigor.main.user.common.core.repository.UserRepository;
import com.binaryigor.main.user.common.core.repository.UserUpdateRepository;
import com.binaryigor.tools.Records;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryUserRepository implements UserRepository, UserUpdateRepository {

    private final Map<UUID, User> db = new ConcurrentHashMap<>();

    @Override
    public UUID create(User user) {
        db.put(user.id(), user);
        return user.id();
    }

    @Override
    public Optional<User> ofEmail(String email) {
        return db.values().stream().filter(u -> u.email().equals(email)).findAny();
    }

    @Override
    public Optional<User> ofId(UUID id) {
        return Optional.ofNullable(db.get(id));
    }

    @Override
    public void updateName(UUID id, String name) {
        ofId(id).ifPresent(u -> {
            db.put(id, Records.copy(u, Map.of("name", name)));
        });
    }

    @Override
    public void updateEmail(UUID id, String email) {
        ofId(id).ifPresent(u -> {
            db.put(id, Records.copy(u, Map.of("email", email)));
        });
    }

    @Override
    public void updateState(UUID id, UserState state) {
        ofId(id).ifPresent(u -> {
            db.put(id, Records.copy(u, Map.of("state", state)));
        });
    }

    @Override
    public void updatePassword(UUID id, String password) {
        ofId(id).ifPresent(u -> {
            db.put(id, Records.copy(u, Map.of("password", password)));
        });
    }
}
