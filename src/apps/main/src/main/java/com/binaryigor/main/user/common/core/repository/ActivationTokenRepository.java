package com.binaryigor.main.user.common.core.repository;

import com.binaryigor.main.user.common.core.model.ActivationToken;
import com.binaryigor.main.user.common.core.model.ActivationTokenId;

import java.util.Optional;

public interface ActivationTokenRepository {

    void save(ActivationToken token);

    Optional<ActivationToken> ofId(ActivationTokenId id);

    void delete(ActivationTokenId id);
}
