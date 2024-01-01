package com.binaryigor.main.user.common.core;

import com.binaryigor.main.user.common.core.model.ActivationToken;
import com.binaryigor.main.user.common.core.model.ActivationTokenType;
import com.binaryigor.tools.DataTokens;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public class ActivationTokenFactory {

    private final TokenFactory tokenFactory;
    private final Clock clock;

    public ActivationTokenFactory(TokenFactory tokenFactory,
                                  Clock clock) {
        this.tokenFactory = tokenFactory;
        this.clock = clock;
    }

    public ActivationTokenFactory(Clock clock) {
        this(DataTokens::containing, clock);
    }

    public ActivationToken newUser(UUID userId) {
        var token = tokenFactory.newToken(ActivationTokenData.withUserId(userId));

        return ActivationToken.ofInitialStatus(userId, ActivationTokenType.NEW_USER, token,
                tokenExpiresAt(Duration.ofMinutes(15)));
    }

    private Instant tokenExpiresAt(Duration duration) {
        return clock.instant().plus(duration);
    }

    public ActivationToken newEmail(UUID userId, String newEmail) {
        var token = tokenFactory.newToken(ActivationTokenData.withUserIdAndNewEmail(userId, newEmail));

        return ActivationToken.ofInitialStatus(userId, ActivationTokenType.EMAIL_CHANGE, token,
                tokenExpiresAt(Duration.ofHours(1)));
    }

    public ActivationToken passwordReset(UUID userId) {
        var token = tokenFactory.newToken(ActivationTokenData.withUserId(userId));

        return ActivationToken.ofInitialStatus(userId, ActivationTokenType.PASSWORD_RESET, token,
                tokenExpiresAt(Duration.ofHours(1)));
    }

    public interface TokenFactory {
        String newToken(ActivationTokenData data);
    }
}
