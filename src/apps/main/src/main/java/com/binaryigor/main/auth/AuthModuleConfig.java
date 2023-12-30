package com.binaryigor.main.auth;

import com.binaryigor.main._commons.core.PropertiesConverter;
import com.binaryigor.main._contract.UserAuthClient;
import com.binaryigor.main.auth.app.JwtConfig;
import com.binaryigor.main.auth.app.SecurityEndpoints;
import com.binaryigor.main.auth.app.SecurityFilter;
import com.binaryigor.main.auth.app.SecurityRules;
import com.binaryigor.main.auth.core.AuthTokenAuthenticator;
import com.binaryigor.main.auth.core.JwtAuthTokens;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.Duration;

@Configuration
@EnableConfigurationProperties(JwtConfig.class)
public class AuthModuleConfig {

    //TODO: admin tests!
    @Bean
    SecurityRules securityRules() {
        return new SecurityRules(new SecurityRules.Predicates(
                SecurityEndpoints::isPublic,
                SecurityEndpoints::isUserOfStateAllowed,
                //TODO: proper impl once we have ban mechanism
                e -> false,
                SecurityEndpoints::isAdmin));
    }

    @Bean
    JwtAuthTokens jwtAuthTokens(UserAuthClient userAuthClient,
                                JwtConfig config,
                                Clock clock) {
        var readTokenKey = PropertiesConverter.valueOrFromFile(config.tokenKey());
        var bytesTokenKey = PropertiesConverter.bytesFromString(readTokenKey);

        var componentConfig = new JwtAuthTokens.Config(
                config.issuer(),
                bytesTokenKey,
                config.tokenDuration(),
                clock);

        return new JwtAuthTokens(userAuthClient, componentConfig);
    }

    @Bean
    SecurityFilter securityFilter(AuthTokenAuthenticator authTokenAuthenticator,
                                  SecurityRules securityRules,
                                  Clock clock,
                                  @Value("${jwt-issue-new-token-before-expiration-duration}")
                                  Duration issueNewTokenBeforeExpirationDuration) {
        return new SecurityFilter(authTokenAuthenticator, securityRules, clock,
                issueNewTokenBeforeExpirationDuration);
    }
}
