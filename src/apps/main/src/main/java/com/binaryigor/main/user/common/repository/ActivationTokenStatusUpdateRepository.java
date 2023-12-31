package com.binaryigor.main.user.common.repository;

import com.binaryigor.main.user.common.model.ActivationTokenId;
import com.binaryigor.main.user.common.model.ActivationTokenStatus;

public interface ActivationTokenStatusUpdateRepository {
    void update(ActivationTokenId id, ActivationTokenStatus status);
}
