package com.binaryigor.email;

import com.binaryigor.email.model.EmailTemplate;
import com.binaryigor.email.model.EmailTemplates;
import com.binaryigor.email.model.EmailTranslation;
import com.binaryigor.test.TestRandom;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EmailTemplatesValidatorTest {

    private static final List<String> REQUIRED_LANGUAGES = List.of("PL", "EN");

    @ParameterizedTest
    @MethodSource("invalidCases")
    void shouldThrowExceptionGivenInvalidTemplates(EmailTemplates templates,
                                                   Map<String, List<String>> templatesAllowedVariables,
                                                   String exception) {
        Assertions.assertThatThrownBy(() -> EmailTemplatesValidator.validate(templates, templatesAllowedVariables,
                        REQUIRED_LANGUAGES))
                .hasMessage(exception);
    }

    static Stream<Arguments> invalidCases() {
        var nullGlobalVariables = Arguments.of(new EmailTemplates(Map.of(), null, Map.of()),
                Map.of(), "Null global variables, they need to be at least empty");

        var nullGlobalTranslations = Arguments.of(new EmailTemplates(Map.of(), Map.of(), null),
                Map.of(), "Null global translations, they need to be at least empty");

        var emptyHtmlTemplate = new EmailTemplate(List.of(), Map.of(), Map.of(), "", "some text");
        var nullHtmlTemplate = new EmailTemplate(List.of(), Map.of(), Map.of(), null, "some text");
        var emptyTextTemplate = new EmailTemplate(List.of(), Map.of(), Map.of(), "Some html", "");
        var nullTextTemplate = new EmailTemplate(List.of(), Map.of(), Map.of(), "Some html", null);
        var invalidTemplate = "some-template";

        var codeVariables = List.of("A", "B");

        var validTemplateName = "some-valid-template";
        var validTemplates = singleTemplateEmailTemplates(validTemplateName,
                new EmailTemplate(codeVariables, Map.of(), Map.of(), "some html", "some text"));

        var differentLengthTemplateVariables = new ArrayList<>(codeVariables);
        differentLengthTemplateVariables.add("C");

        var differentTemplateVariables = List.of("A", "B8x");

        var lackingVariableTemplateName = "lacking-variable-template";
        var lackingVariableTemplates = singleTemplateEmailTemplates(lackingVariableTemplateName,
                new EmailTemplate(codeVariables, allLanguagesEmailTranslations(), Map.of("var1", "some -value"),
                        "html: we need ${var2}", "text: we need ${var2}"));

        return Stream.of(nullGlobalVariables, nullGlobalTranslations,
                Arguments.of(singleTemplateEmailTemplates(invalidTemplate, emptyHtmlTemplate),
                        Map.of(), "some-template html doesn't have any content"),
                Arguments.of(singleTemplateEmailTemplates(invalidTemplate, nullHtmlTemplate),
                        Map.of(), "some-template html doesn't have any content"),
                Arguments.of(singleTemplateEmailTemplates(invalidTemplate, emptyTextTemplate),
                        Map.of(), "some-template text doesn't have any content"),
                Arguments.of(singleTemplateEmailTemplates(invalidTemplate, nullTextTemplate),
                        Map.of(), "some-template text doesn't have any content"),
                Arguments.of(validTemplates, Map.of(validTemplateName, differentLengthTemplateVariables),
                        "Mismatched variables for some-valid-template template. Template: [A, B], code: [A, B, C]"),
                Arguments.of(validTemplates, Map.of(validTemplateName, differentTemplateVariables),
                        "Mismatched variables for some-valid-template template. Template: [A, B], code: [A, B8x]"),
                Arguments.of(lackingVariableTemplates, Map.of(lackingVariableTemplateName, codeVariables),
                        "var2 variable is not present neither in template or global variables, but is required for lacking-variable-template template"));
    }

    private static EmailTemplates singleTemplateEmailTemplates(String templateName, EmailTemplate template) {
        return new EmailTemplates(Map.of(templateName, template), Map.of(), Map.of());
    }

    private static Map<String, EmailTranslation> allLanguagesEmailTranslations() {
        return REQUIRED_LANGUAGES.stream()
                .collect(Collectors.toMap(Function.identity(),
                        l -> new EmailTranslation(TestRandom.string(),
                                Map.of(TestRandom.name(), TestRandom.string()))));
    }
}
