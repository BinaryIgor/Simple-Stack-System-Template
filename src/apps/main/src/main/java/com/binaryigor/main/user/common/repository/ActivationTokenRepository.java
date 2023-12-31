package com.binaryigor.main.user.common.repository;

import com.binaryigor.main.user.common.model.ActivationToken;
import com.binaryigor.main.user.common.model.ActivationTokenId;

import java.util.Optional;

public interface ActivationTokenRepository {

    void save(ActivationToken token);

    Optional<ActivationToken> ofId(ActivationTokenId id);

    void delete(ActivationTokenId id);
}
