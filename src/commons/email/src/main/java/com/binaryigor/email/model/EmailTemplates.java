package com.binaryigor.email.model;

import java.util.Map;
import java.util.Optional;

public record EmailTemplates(Map<String, EmailTemplate> templates,
                             Map<String, String> globalVariables,
                             Map<String, Map<String, String>> globalTranslations) {

    public Optional<EmailTemplate> templateOfType(String type) {
        return Optional.ofNullable(templates.get(type));
    }
}
