package com.binaryigor.main.user;

import com.binaryigor.main._commons.core.AppLanguage;
import com.binaryigor.main._contract.model.UserState;
import com.binaryigor.main.user.common.core.DumbPasswordHasher;
import com.binaryigor.main.user.common.core.PasswordHasher;
import com.binaryigor.main.user.common.core.model.User;
import com.binaryigor.main.user.common.core.repository.UserRepository;
import com.binaryigor.main.user.common.infra.InMemoryUserRepository;
import com.binaryigor.main.user.common.infra.SqlUserClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;
import java.util.UUID;

@Configuration
public class UserModuleConfig {

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
}
