package com.binaryigor.main.user.common;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "email.user")
public record UserEmailProperties(String userActivationUrl,
                                  String signUpUrl,
                                  String emailChangeConfirmationUrl,
                                  String passwordResetUrl,
                                  String newPasswordUrl) {
}
