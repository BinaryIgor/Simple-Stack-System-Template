package com.binaryigor.email.factory;

import com.binaryigor.email.model.Email;
import com.binaryigor.email.model.EmailTemplates;
import com.binaryigor.email.model.NewEmailTemplate;
import com.binaryigor.tools.Templates;

import java.util.HashMap;
import java.util.Map;

public class TemplatesEmailFactory implements EmailFactory {

    private static final String LOCALE_VAR = "language";
    private final EmailTemplates templates;

    public TemplatesEmailFactory(EmailTemplates templates) {
        this.templates = templates;
    }

    public Email newEmail(NewEmailTemplate template) {
        var emailTemplate = templates.templateOfType(template.name())
                .orElseThrow(() -> new RuntimeException("%s template doesn't exist".formatted(template.name())));

        var emailTranslation = emailTemplate.translation(template.language());

        var allVariables = mergedVariables(template.language(), template.variables(),
                emailTemplate.templateVariables(), emailTranslation.messages());

        var renderedSubject = Templates.rendered(emailTranslation.subject(), allVariables);
        var renderedHtml = Templates.rendered(emailTemplate.html(), allVariables);
        var renderedText = Templates.rendered(emailTemplate.text(), allVariables);

        return new Email(template.from(), template.to(), renderedSubject, renderedHtml, renderedText,
                template.emailTag(), template.emailMetadata());
    }

    private Map<String, String> mergedVariables(String language,
                                                Map<String, String> dataVariables,
                                                Map<String, String> templateVariables,
                                                Map<String, String> messageVariables) {
        var allVariables = new HashMap<>(templates.globalVariables());

        allVariables.put(LOCALE_VAR, language.toLowerCase());
        allVariables.putAll(dataVariables);
        allVariables.putAll(templateVariables);

        var allMessageVariables = new HashMap<>(templates.globalTranslations().get(language));
        allMessageVariables.putAll(messageVariables);

        for (var e : allMessageVariables.entrySet()) {
            var substituted = Templates.rendered(e.getValue(), allVariables);
            allVariables.put(e.getKey(), substituted);
        }

        return allVariables;
    }
}
