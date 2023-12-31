package com.binaryigor.main.user.auth;

import com.binaryigor.main._contract.AuthClient;
import com.binaryigor.main.user.auth.core.SecondFactorAuthenticator;
import com.binaryigor.main.user.auth.core.handler.SignInFirstStepHandler;
import com.binaryigor.main.user.auth.infra.InMemorySecondFactorAuthenticationRepository;
import com.binaryigor.main.user.common.core.PasswordHasher;
import com.binaryigor.main.user.common.core.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class UserAuthModuleConfig {


    @Bean
    SecondFactorAuthenticator secondFactorAuthenticator(Clock clock) {
        return new SecondFactorAuthenticator(new InMemorySecondFactorAuthenticationRepository(), clock);
    }

    @Bean
    SignInFirstStepHandler signInFirstStepHandler(AuthClient authClient,
                                                  UserRepository userRepository,
                                                  PasswordHasher passwordHasher,
                                                  SecondFactorAuthenticator secondFactorAuthenticator) {
        return new SignInFirstStepHandler(authClient, userRepository, passwordHasher,
                secondFactorAuthenticator);
    }
}
