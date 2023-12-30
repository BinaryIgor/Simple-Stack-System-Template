package com.binaryigor.main.auth.core;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.binaryigor.main._commons.exception.InvalidAuthTokenException;
import com.binaryigor.main._contract.UserAuthClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

//TODO: simplify & test

public class JwtAuthTokens implements AuthTokenCreator, AuthTokenAuthenticator {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthTokens.class);
    private final UserAuthClient userAuthClient;
    private final Clock clock;
    private final String issuer;
    private final Algorithm algorithm;
    private final Duration tokenDuration;

    public JwtAuthTokens(UserAuthClient userAuthClient, Config config) {
        this.userAuthClient = userAuthClient;
        this.clock = config.clock;
        this.issuer = config.issuer;
        this.algorithm = config.algorithm;
        this.tokenDuration = config.tokenDuration;
    }

    static String newToken(String issuer,
                           UUID subject,
                           Instant issuedAt,
                           Instant expiresAt,
                           Algorithm algorithm) {
        return JWT.create()
                .withIssuer(issuer)
                .withSubject(subject.toString())
                .withIssuedAt(issuedAt)
                .withExpiresAt(expiresAt)
                .sign(algorithm);
    }

    @Override
    public AuthenticationResult authenticate(String token) {
        return validateToken(token);
    }

    @Override
    public AuthToken ofUser(UUID id) {
        var issuedAt = clock.instant();
        return token(id, issuedAt);
    }

    private AuthToken token(UUID id, Instant issuedAt) {
        var expiresAt = issuedAt.plus(tokenDuration);
        var token = newToken(issuer, id, issuedAt, expiresAt, algorithm);
        return new AuthToken(token, expiresAt);
    }

    @Override
    public AuthToken refresh(String token) {
        var result = validateToken(token);
        return ofUser(result.user().id());
    }

    private AuthenticationResult validateToken(String token) {
        UUID userId;
        Instant expiresAt;

        try {
            var decodedToken = tokenVerifier().verify(token);
            userId = UUID.fromString(decodedToken.getSubject());
            expiresAt = decodedToken.getExpiresAtAsInstant();
        } catch (Exception e) {
            log.warn("Invalid token", e);
            throw new InvalidAuthTokenException("Invalid token");
        }

        return userAuthClient.dataOfId(userId)
                .map(d -> {
                    var u = d.toAuthenticatedUser();
                    return new AuthenticationResult(u, expiresAt);
                })
                .orElseThrow(() -> new InvalidAuthTokenException("User %s doesn't exist".formatted(userId)));
    }

    private JWTVerifier tokenVerifier() {
        var builder = JWT.require(algorithm)
                .withIssuer(issuer);

        if (builder instanceof JWTVerifier.BaseVerification b) {
            return b.build(clock);
        }

        return builder.build();
    }

    /*
            https://crypto.stackexchange.com/questions/53826/hmac-sha256-vs-hmac-sha512-for-jwt-api-authentication
            Both algorithms provide plenty of security, near the output size of the hash.
            So even though HMAC-512 will be stronger, the difference is inconsequential.
            If this ever breaks it is because the algorithm itself is broken and as both hash algorithms are related, it is likely that both would be in trouble.
            However, no such attack is known and the HMAC construct itself appears to be very strong indeed.

            SHA-512 is indeed faster than SHA-256 on 64 bit machines.
            It may be that the overhead provided by the block size of SHA-512 is detrimental to HMAC-ing short length message sizes. But you can speedup larger messages sizes using HMAC-SHA-512 for sure.
            Then again, SHA-256 is plenty fast itself, and is faster on 32 bit and lower machines, so I'd go for HMAC-SHA-256 if lower end machines could be involved.

            Note that newer x86 processors also contain SHA-1 and SHA-256 accelerator hardware, so that may shift the speed advantage back into SHA-256's favor compared to SHA-512.
             */
    public record Config(
            String issuer,
            Algorithm algorithm,
            Duration tokenDuration,
            Clock clock) {

        public Config(String issuer,
                      byte[] tokenKey,
                      Duration tokenDuration,
                      Clock clock) {
            this(issuer, Algorithm.HMAC512(tokenKey), tokenDuration, clock);
        }
    }
}
