package com.binaryigor.main.user.common.infra;

import com.binaryigor.main.user.common.core.model.User;
import com.binaryigor.main.user.common.core.repository.UserRepository;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryUserRepository implements UserRepository {

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
}
