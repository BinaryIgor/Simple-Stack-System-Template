package com.binaryigor.main.user.common.core;

import com.binaryigor.main._common.core.Emails;
import com.binaryigor.main.user.common.core.model.ActivationTokenId;
import com.binaryigor.main.user.common.core.model.ActivationTokenStatus;
import com.binaryigor.main.user.common.core.model.ActivationTokenType;
import com.binaryigor.main.user.common.core.repository.ActivationTokenStatusUpdateRepository;
import com.binaryigor.types.Pair;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class UserEmailStatusHandler {

    private final ActivationTokenStatusUpdateRepository statusUpdateRepository;

    public UserEmailStatusHandler(ActivationTokenStatusUpdateRepository statusUpdateRepository) {
        this.statusUpdateRepository = statusUpdateRepository;
    }

    public void handleDelivery(Map<String, String> emailMetadata) {
        updateActivationTokenStatusIfNeeded(emailMetadata, ActivationTokenStatus.DELIVERED);
    }

    private Optional<Pair<UUID, ActivationTokenType>> extractedUserIdAndTokenType(Map<String, String> metadata) {
        return Emails.Metadata.userId(metadata)
                .flatMap(uid ->
                        Emails.Metadata.activationTokenType(metadata)
                                .map(t -> new Pair<>(uid, t)));
    }

    private void updateActivationTokenStatusIfNeeded(Map<String, String> emailMetadata,
                                                     ActivationTokenStatus newStatus) {
        extractedUserIdAndTokenType(emailMetadata)
                .ifPresent(p -> {
                    var userId = p.first();
                    var tokenType = p.second();
                    statusUpdateRepository.update(new ActivationTokenId(userId, tokenType), newStatus);
                });
    }

    public void handleBounce(Map<String, String> emailMetadata) {
        updateActivationTokenStatusIfNeeded(emailMetadata, ActivationTokenStatus.DELIVERY_FAILURE);
    }
}
