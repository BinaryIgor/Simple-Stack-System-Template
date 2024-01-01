package com.binaryigor.email.model;

import java.util.Map;

public record NewEmailTemplate(EmailAddress from,
                               EmailAddress to,
                               String language,
                               String name,
                               Map<String, String> variables,
                               String emailTag,
                               Map<String, String> emailMetadata) {
}
