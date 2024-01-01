package com.binaryigor.main.user.common;

import com.binaryigor.email.factory.EmailFactory;
import com.binaryigor.email.server.EmailServer;
import com.binaryigor.email.server.PostmarkEmailStatusHandler;
import com.binaryigor.main.EmailProperties;
import com.binaryigor.main._common.core.AppLanguage;
import com.binaryigor.main._contract.model.UserState;
import com.binaryigor.main.user.common.core.*;
import com.binaryigor.main.user.common.core.model.User;
import com.binaryigor.main.user.common.core.repository.ActivationTokenRepository;
import com.binaryigor.main.user.common.core.repository.ActivationTokenStatusUpdateRepository;
import com.binaryigor.main.user.common.core.repository.UserRepository;
import com.binaryigor.main.user.common.infra.InMemoryActivationTokenRepository;
import com.binaryigor.main.user.common.infra.InMemoryUserRepository;
import com.binaryigor.main.user.common.infra.SqlUserClient;
import com.binaryigor.types.Transactions;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Configuration
@EnableConfigurationProperties(UserEmailProperties.class)
public class UserCommonModuleConfig {

    @Bean
    SqlUserClient userClient(UserRepository userRepository) {
        return new SqlUserClient(userRepository);
    }

    @Bean
    PasswordHasher passwordHasher() {
        return new DumbPasswordHasher();
    }

    @Bean
    UserRepository userRepository() {
        var repository = new InMemoryUserRepository();

        //TODO: remove!
        repository.create(new User(UUID.fromString("4644ca6f-8bbb-413f-9688-10b5692b5bcf"),
                "Igor", "igor@binaryigor.com",
                AppLanguage.EN, UserState.ACTIVATED, Set.of(),
                "ComplexPassword1", false));

        return repository;
    }

    @Bean
    UserEmailSender userEmailSender(EmailFactory emailFactory,
                                    EmailServer emailServer,
                                    EmailProperties emailProperties,
                                    UserEmailProperties userEmailProperties) {
        return new UserEmailSender(emailFactory, emailServer,
                new UserEmailSender.Config(emailProperties.frontendDomain(),
                        emailProperties.fromEmail(),
                        userEmailProperties.userActivationUrl(),
                        userEmailProperties.signUpUrl(),
                        userEmailProperties.emailChangeConfirmationUrl(),
                        userEmailProperties.passwordResetUrl(),
                        userEmailProperties.newPasswordUrl()));
    }

    @Bean
    InMemoryActivationTokenRepository activationTokenRepository() {
        return new InMemoryActivationTokenRepository();
    }

    @Bean
    ActivationTokenFactory activationTokenFactory(Clock clock) {
        return new ActivationTokenFactory(clock);
    }

    @Bean
    ActivationTokens activationTokens(ActivationTokenRepository activationTokenRepository,
                                      ActivationTokenFactory activationTokenFactory) {
        return new ActivationTokens(activationTokenRepository, activationTokenFactory);
    }

    @Bean
    ActivationTokenConsumer activationTokenConsumer(ActivationTokenRepository activationTokenRepository,
                                                    Transactions transactions) {
        return new ActivationTokenConsumer(activationTokenRepository, transactions);
    }

    @Bean
    UserEmailStatusHandler userEmailStatusHandler(
            ActivationTokenStatusUpdateRepository activationTokenStatusUpdateRepository) {
        return new UserEmailStatusHandler(activationTokenStatusUpdateRepository);
    }

    @Bean
    PostmarkEmailStatusHandler postmarkEmailStatusHandler(UserEmailStatusHandler emailStatusHandler) {
        return new PostmarkEmailStatusHandler(new PostmarkEmailStatusHandler.Actions() {
            @Override
            public void onBounce(Map<String, String> emailMetadata) {
                emailStatusHandler.handleBounce(emailMetadata);
            }

            @Override
            public void onDelivery(Map<String, String> emailMetadata) {
                emailStatusHandler.handleDelivery(emailMetadata);
            }
        });
    }
}
