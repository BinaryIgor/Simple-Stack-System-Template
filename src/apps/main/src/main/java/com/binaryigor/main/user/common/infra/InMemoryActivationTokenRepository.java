package com.binaryigor.main.user.common.infra;

import com.binaryigor.main.user.common.core.model.ActivationToken;
import com.binaryigor.main.user.common.core.model.ActivationTokenId;
import com.binaryigor.main.user.common.core.model.ActivationTokenStatus;
import com.binaryigor.main.user.common.core.repository.ActivationTokenRepository;
import com.binaryigor.main.user.common.core.repository.ActivationTokenStatusUpdateRepository;
import com.binaryigor.tools.Records;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryActivationTokenRepository implements ActivationTokenRepository, ActivationTokenStatusUpdateRepository {

    private final Map<ActivationTokenId, ActivationToken> db = new ConcurrentHashMap<>();

    @Override
    public void save(ActivationToken token) {
        db.put(token.id(), token);
    }

    @Override
    public Optional<ActivationToken> ofId(ActivationTokenId id) {
        return Optional.ofNullable(db.get(id));
    }

    @Override
    public void delete(ActivationTokenId id) {
        db.remove(id);
    }

    @Override
    public void update(ActivationTokenId id, ActivationTokenStatus status) {
        ofId(id).ifPresent(at -> {
            db.put(id, Records.copy(at, Map.of("status", status)));
        });
    }
}
