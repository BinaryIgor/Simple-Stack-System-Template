package com.binaryigor.main.user.common.core;

import com.binaryigor.main.user.common.core.exception.InvalidActivationTokenException;
import com.binaryigor.main.user.common.core.model.ActivationTokenId;
import com.binaryigor.main.user.common.core.model.ActivationTokenType;
import com.binaryigor.main.user.common.core.repository.ActivationTokenRepository;
import com.binaryigor.tools.DataTokens;
import com.binaryigor.types.Transactions;

import java.time.Instant;
import java.util.UUID;
import java.util.function.Consumer;

//TODO: tests
public class ActivationTokenConsumer {

    private final ActivationTokenRepository activationTokenRepository;
    private final Transactions transactions;

    public ActivationTokenConsumer(ActivationTokenRepository activationTokenRepository,
                                   Transactions transactions) {
        this.activationTokenRepository = activationTokenRepository;
        this.transactions = transactions;
    }

    public void consume(String activationToken, ActivationTokenType tokenType, Consumer<UUID> onValidToken) {
        consumeWithData(activationToken, tokenType, data -> onValidToken.accept(data.userId()));
    }


    public void consumeWithData(String activationToken,
                                ActivationTokenType tokenType,
                                Consumer<ActivationTokenData> onValidToken) {
        var tokenData = activationTokenData(activationToken);

        var userId = tokenData.userId();
        var tokenId = new ActivationTokenId(userId, tokenType);

        validateToken(tokenId, activationToken);

        transactions.execute(() -> {
            onValidToken.accept(tokenData);
            activationTokenRepository.delete(tokenId);
        });
    }

    private ActivationTokenData activationTokenData(String token) {
        try {
            return DataTokens.extractedData(token, ActivationTokenData.class);
        } catch (Exception e) {
            throw InvalidActivationTokenException.ofToken(token);
        }
    }

    private void validateToken(ActivationTokenId id, String receivedToken) {
        var token = activationTokenRepository.ofId(id)
                .orElseThrow(() -> UserExceptions.activationTokenNotFound(id));

        if (!receivedToken.equals(token.token())) {
            throw InvalidActivationTokenException.ofToken(id, "Received token is not equal to saved one");
        }

        if (Instant.now().isAfter(token.expiresAt())) {
            throw InvalidActivationTokenException.ofToken(id, "Token has expired");
        }
    }
}
