package com.binaryigor.main.auth;

import com.binaryigor.main._common.app.Cookies;
import com.binaryigor.main._contract.AuthClient;
import com.binaryigor.main._contract.UserAuthClient;
import com.binaryigor.main.auth.app.JwtConfig;
import com.binaryigor.main.auth.app.SecurityEndpoints;
import com.binaryigor.main.auth.app.SecurityFilter;
import com.binaryigor.main.auth.app.SecurityRules;
import com.binaryigor.main.auth.core.AuthTokenAuthenticator;
import com.binaryigor.main.auth.core.AuthTokenCreator;
import com.binaryigor.main.auth.core.JwtAuthTokens;
import com.binaryigor.main.auth.core.TheAuthClient;
import com.binaryigor.tools.PropertiesConverter;
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
                                  Cookies cookies,
                                  Clock clock,
                                  @Value("${jwt-issue-new-token-before-expiration-duration}")
                                  Duration issueNewTokenBeforeExpirationDuration) {
        return new SecurityFilter(authTokenAuthenticator, securityRules, cookies, clock,
                issueNewTokenBeforeExpirationDuration);
    }

    @Bean
    TheAuthClient authClient(AuthTokenCreator authTokenCreator) {
        return new TheAuthClient(authTokenCreator);
    }
}
