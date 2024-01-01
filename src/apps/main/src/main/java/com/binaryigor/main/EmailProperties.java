package com.binaryigor.main;

import com.binaryigor.email.model.EmailAddress;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "email")
public record EmailProperties(boolean fakeServer,
                              String templatesDir,
                              String postmarkApiToken,
                              String postmarkWebhookToken,
                              String frontendDomain,
                              EmailAddress fromEmail) {
}
