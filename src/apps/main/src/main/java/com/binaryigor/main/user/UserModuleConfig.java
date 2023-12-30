package com.binaryigor.main.user;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserModuleConfig {

    @Bean
    DefaultUserClient userClient() {
        return new DefaultUserClient();
    }
}
