package com.binaryigor.main.user.common.core.repository;

import com.binaryigor.main.user.common.core.model.ActivationTokenId;
import com.binaryigor.main.user.common.core.model.ActivationTokenStatus;

public interface ActivationTokenStatusUpdateRepository {
    void update(ActivationTokenId id, ActivationTokenStatus status);
}
