package com.binaryigor.main.user.common;

import com.binaryigor.email.factory.EmailFactory;
import com.binaryigor.email.server.EmailServer;
import com.binaryigor.main.EmailProperties;
import com.binaryigor.main._common.core.AppLanguage;
import com.binaryigor.main._contract.model.UserState;
import com.binaryigor.main.user.common.core.DumbPasswordHasher;
import com.binaryigor.main.user.common.core.PasswordHasher;
import com.binaryigor.main.user.common.core.UserEmailSender;
import com.binaryigor.main.user.common.core.model.User;
import com.binaryigor.main.user.common.core.repository.UserRepository;
import com.binaryigor.main.user.common.infra.InMemoryUserRepository;
import com.binaryigor.main.user.common.infra.SqlUserClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
}
