package com.binaryigor.email.factory;

import com.binaryigor.email.EmailTemplatesSource;
import com.binaryigor.email.model.Email;
import com.binaryigor.email.model.EmailAddress;
import com.binaryigor.email.model.NewEmailTemplate;
import com.binaryigor.test.TestDataLoader;
import com.binaryigor.test.TestRandom;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Map;

public class TemplatesEmailFactoryTest {

    private static final List<String> REQUIRED_LANGUAGES = List.of("PL", "EN");
    private TemplatesEmailFactory factory;

    @BeforeEach
    void setup() {
        var templates = EmailTemplatesSource.fromFiles(TestDataLoader.classpathFile("email_factory/template"),
                REQUIRED_LANGUAGES);
        factory = new TemplatesEmailFactory(templates);
    }

    @ParameterizedTest
    @ValueSource(strings = {"email_change", "user_activation"})
    void shouldRenderNewEmailGivenTemplate(String template) {
        var testCase = prepareNewEmailTestCase(template);

        Assertions.assertThat(factory.newEmail(testCase.input))
                .isEqualTo(testCase.output);
    }

    @Test
    void shouldThrowExceptionGivenNonExistingEmailTemplate() {
        var template = "non-existing-template";

        var newEmailTemplate = new NewEmailTemplate(EmailAddress.ofEmptyName("email@email.com"),
                EmailAddress.ofEmptyName("admin@admin.io"),
                TestRandom.oneOf(REQUIRED_LANGUAGES),
                template,
                Map.of(),
                "Tagging",
                Map.of("meta", "112"));

        Assertions.assertThatThrownBy(() -> factory.newEmail(newEmailTemplate))
                .hasMessageContaining("non-existing-template template doesn't exist");
    }

    private NewEmailTestCase prepareNewEmailTestCase(String template) {
        var prefix = "email_factory/%s/".formatted(template);
        var testCase = TestDataLoader.object(prefix + "testCase.json", NewEmailTestCase.class);

        var htmlEmail = TestDataLoader.classpathResourceContent(prefix + "email.html");
        var txtEmail = TestDataLoader.classpathResourceContent(prefix + "email.txt");

        var email = new Email(testCase.output.from(), testCase.output.to(), testCase.output.subject(),
                htmlEmail, txtEmail, testCase.input.emailTag(), testCase.input.emailMetadata());

        return new NewEmailTestCase(testCase.input, email);
    }

    private record NewEmailTestCase(NewEmailTemplate input, Email output) {
    }

}
