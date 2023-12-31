package com.binaryigor.main.user.home;

import com.binaryigor.main.user.common.core.repository.UserRepository;
import com.binaryigor.main.user.home.core.GetCurrentUserDataHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserHomeModuleConfig {

    @Bean
    GetCurrentUserDataHandler getCurrentUserDataHandler(UserRepository userRepository) {
        return new GetCurrentUserDataHandler(userRepository);
    }
}
