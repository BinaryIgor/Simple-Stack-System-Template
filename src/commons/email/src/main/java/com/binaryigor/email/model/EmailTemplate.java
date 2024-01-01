package com.binaryigor.email.model;

import java.util.List;
import java.util.Map;

public record EmailTemplate(List<String> codeVariables,
                            Map<String, EmailTranslation> translations,
                            Map<String, String> templateVariables,
                            String html,
                            String text) {

    public EmailTemplate withHtmlAndText(String html, String text) {
        return new EmailTemplate(codeVariables, translations, templateVariables, html, text);
    }

    public EmailTranslation translation(String language) {
        return translations.get(language);
    }
}
