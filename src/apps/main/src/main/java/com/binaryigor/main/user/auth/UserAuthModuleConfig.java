package com.binaryigor.main.user.auth;

import com.binaryigor.main._contract.AuthClient;
import com.binaryigor.main.user.auth.core.SecondFactorAuthenticator;
import com.binaryigor.main.user.auth.core.handler.ActivateUserHandler;
import com.binaryigor.main.user.auth.core.handler.SignInFirstStepHandler;
import com.binaryigor.main.user.auth.core.handler.SignUpHandler;
import com.binaryigor.main.user.auth.infra.InMemorySecondFactorAuthenticationRepository;
import com.binaryigor.main.user.common.core.ActivationTokenConsumer;
import com.binaryigor.main.user.common.core.ActivationTokens;
import com.binaryigor.main.user.common.core.PasswordHasher;
import com.binaryigor.main.user.common.core.UserEmailSender;
import com.binaryigor.main.user.common.core.repository.UserRepository;
import com.binaryigor.main.user.common.core.repository.UserUpdateRepository;
import com.binaryigor.types.Transactions;
import com.binaryigor.types.event.AppEventsPublisher;
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
    SignUpHandler signUpHandler(UserRepository userRepository,
                                PasswordHasher passwordHasher,
                                ActivationTokens activationTokens,
                                UserEmailSender userEmailSender,
                                Transactions transactions) {
        return new SignUpHandler(userRepository, passwordHasher, activationTokens, userEmailSender, transactions);
    }

    @Bean
    ActivateUserHandler activateUserHandler(ActivationTokenConsumer activationTokenConsumer,
                                            UserUpdateRepository userUpdateRepository,
                                            AppEventsPublisher appEventsPublisher) {
        return new ActivateUserHandler(activationTokenConsumer, userUpdateRepository, appEventsPublisher);
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
