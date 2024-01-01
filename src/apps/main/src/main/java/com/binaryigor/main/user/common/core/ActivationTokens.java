package com.binaryigor.main.user.common.core;

import com.binaryigor.main.user.common.core.model.ActivationToken;
import com.binaryigor.main.user.common.core.repository.ActivationTokenRepository;

import java.util.UUID;

public class ActivationTokens {

    private final ActivationTokenRepository tokenRepository;
    private final ActivationTokenFactory tokenFactory;

    public ActivationTokens(ActivationTokenRepository tokenRepository,
                            ActivationTokenFactory tokenFactory) {
        this.tokenRepository = tokenRepository;
        this.tokenFactory = tokenFactory;
    }

    public ActivationToken saveNewUser(UUID userId) {
        var token = tokenFactory.newUser(userId);
        tokenRepository.save(token);
        return token;
    }

    public ActivationToken saveNewEmail(UUID userId, String newEmail) {
        var token = tokenFactory.newEmail(userId, newEmail);
        tokenRepository.save(token);
        return token;
    }

    public ActivationToken savePasswordReset(UUID userId) {
        var token = tokenFactory.passwordReset(userId);
        tokenRepository.save(token);
        return token;
    }
}
