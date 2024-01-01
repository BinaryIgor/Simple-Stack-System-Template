package com.binaryigor.email;

import com.binaryigor.email.model.EmailTemplate;
import com.binaryigor.email.model.EmailTemplates;
import com.binaryigor.tools.Templates;

import java.util.*;

public class EmailTemplatesValidator {

    public static void validate(EmailTemplates templates,
                                Map<String, List<String>> templatesAllowedVariables,
                                Collection<String> requireLanguages) {
        if (templates.globalVariables() == null) {
            throw new RuntimeException("Null global variables, they need to be at least empty");
        }
        if (templates.globalTranslations() == null) {
            throw new RuntimeException("Null global translations, they need to be at least empty");
        }
        validateTemplates(templates, templatesAllowedVariables, templates.globalVariables(),
                templates.globalTranslations(), requireLanguages);
    }

    private static void validateTemplates(EmailTemplates templates,
                                          Map<String, List<String>> templatesAllowedVariables,
                                          Map<String, String> globalVariables,
                                          Map<String, Map<String, String>> globalTranslations,
                                          Collection<String> requiredLanguages) {
        for (var t : templates.templates().keySet()) {
            var template = templates.templateOfType(t).orElseThrow();

            if (!hasAnyContent(template.html())) {
                throw new RuntimeException("%s html doesn't have any content".formatted(t));
            }
            if (!hasAnyContent(template.text())) {
                throw new RuntimeException("%s text doesn't have any content".formatted(t));
            }

            var templateVariables = templatesAllowedVariables.getOrDefault(t, List.of());

            if (templateVariables.size() != template.codeVariables().size()
                    || !templateVariables.containsAll(template.codeVariables())) {
                throw new RuntimeException("Mismatched variables for %s template. Template: %s, code: %s"
                        .formatted(t, template.codeVariables(), templateVariables));
            }

            for (var l : requiredLanguages) {
                var lGlobal = globalTranslations.getOrDefault(l, Map.of());
                validateEmailTemplateData(t, template, l, globalVariables, lGlobal);
            }
        }
    }

    private static boolean hasAnyContent(String string) {
        return string != null && !string.isBlank();
    }

    private static void validateEmailTemplateData(String templateName,
                                                  EmailTemplate template,
                                                  String language,
                                                  Map<String, String> globalVariables,
                                                  Map<String, String> globalTranslations) {
        var translation = template.translation(language);
        if (translation == null) {
            throw new RuntimeException(
                    "Null value for %s email template of language %s".formatted(templateName, language));
        }

        if (!hasAnyContent(translation.subject())) {
            throw new RuntimeException("Null or empty subject for %s template".formatted(templateName));
        }

        validateVariables(templateName, template, language, globalVariables, globalTranslations);
    }

    private static void validateVariables(String templateName,
                                          EmailTemplate template,
                                          String language,
                                          Map<String, String> globalVariables,
                                          Map<String, String> globalTranslations) {

        var translation = template.translation(language);

        var availableVariables = new HashSet<>(globalVariables.keySet());
        availableVariables.add("language");
        availableVariables.addAll(template.codeVariables());
        availableVariables.addAll(template.templateVariables().keySet());
        availableVariables.addAll(globalTranslations.keySet());
        availableVariables.addAll(translation.messages().keySet());

        var allTemplatesVariables = new ArrayList<String>();
        allTemplatesVariables.add(translation.subject());

        allTemplatesVariables.addAll(translation.messages().values());
        allTemplatesVariables.addAll(globalTranslations.values());

        allTemplatesVariables.add(template.html());
        allTemplatesVariables.add(template.text());

        var requiredVariables = Templates.variables(allTemplatesVariables);

        for (var rv : requiredVariables) {
            if (!availableVariables.contains(rv)) {
                throwNoRequiredVariableException(rv, templateName);
            }
        }
    }

    private static void throwNoRequiredVariableException(String variable, String template) {
        throw new RuntimeException(
                "%s variable is not present neither in template or global variables, but is required for %s template"
                        .formatted(variable, template));
    }

}
