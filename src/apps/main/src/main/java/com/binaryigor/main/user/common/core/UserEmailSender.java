package com.binaryigor.main.user.common.core;

import com.binaryigor.email.factory.EmailFactory;
import com.binaryigor.email.model.EmailAddress;
import com.binaryigor.email.model.NewEmailTemplate;
import com.binaryigor.email.server.EmailServer;
import com.binaryigor.main._common.core.Emails;
import com.binaryigor.main.user.common.core.model.ActivationTokenType;
import com.binaryigor.main.user.common.core.model.EmailUser;

import java.util.Map;

//TODO: test, config, refactor
public class UserEmailSender {

    private final EmailFactory factory;
    private final EmailServer server;
    private final Config config;

    public UserEmailSender(EmailFactory factory, EmailServer server, Config config) {
        this.factory = factory;
        this.server = server;
        this.config = config;
    }

    public void sendAccountActivation(EmailUser user, String activationToken) {
        var variables = Map.of(Emails.Variables.USER, user.name(),
                Emails.Variables.ACTIVATION_URL,
                fullTokenUrl(config.userActivationUrl(), activationToken, ActivationTokenType.NEW_USER),
                Emails.Variables.SIGN_UP_URL, fullUrl(config.signUpUrl()));

        sendEmail(user, Emails.Types.USER_ACTIVATION, variables, ActivationTokenType.NEW_USER);
    }

    public void sendPasswordReset(EmailUser user, String resetToken) {
        var variables = Map.of(Emails.Variables.USER, user.name(),
                Emails.Variables.NEW_PASSWORD_URL,
                fullTokenUrl(config.newPasswordUrl(), resetToken, ActivationTokenType.PASSWORD_RESET),
                Emails.Variables.PASSWORD_RESET_URL, fullUrl(config.passwordResetUrl()));

        sendEmail(user, Emails.Types.PASSWORD_RESET, variables, ActivationTokenType.PASSWORD_RESET);
    }

    public void sendEmailChange(EmailUser user, String oldEmail, String confirmationToken) {
        var variables = Map.of(Emails.Variables.USER, user.name(),
                Emails.Variables.OLD_EMAIL, oldEmail,
                Emails.Variables.EMAIL_CHANGE_CONFIRMATION_URL,
                fullTokenUrl(config.emailChangeConfirmationUrl(), confirmationToken, ActivationTokenType.EMAIL_CHANGE));

        sendEmail(user, Emails.Types.EMAIL_CHANGE, variables, ActivationTokenType.EMAIL_CHANGE);
    }

    public void sendSecondFactorAuthentication(EmailUser user, String code) {
        var variables = Map.of(Emails.Variables.USER, user.name(),
                Emails.Variables.CODE, code);

        sendEmail(user, Emails.Types.SECOND_FACTOR_AUTHENTICATION, variables, null);
    }

    private String fullTokenUrl(String endpoint, String token, ActivationTokenType type) {
        var url = String.join("/", config.frontendDomain(), endpoint);
        var params = "token=%s&type=%s".formatted(token, type);
        return url + "?" + params;
    }

    private String fullUrl(String part) {
        return String.join("/", config.frontendDomain(), part);
    }


    private void sendEmail(EmailUser user, String type, Map<String, String> variables,
                           ActivationTokenType activationTokenType) {
        Map<String, String> metadata;
        if (activationTokenType == null) {
            metadata = Map.of();
        } else {
            metadata = Emails.Metadata.ofActivationToken(user.id(), activationTokenType.name());
        }

        var emailTemplate = new NewEmailTemplate(config.fromEmail(),
                EmailAddress.ofNameEmail(user.name(), user.email()),
                user.language().name().toLowerCase(),
                type,
                variables,
                type,
                metadata);

        var email = factory.newEmail(emailTemplate);

        server.send(email);
    }

    public record Config(
            String frontendDomain,
            EmailAddress fromEmail,
            String userActivationUrl,
            String signUpUrl,
            String emailChangeConfirmationUrl,
            String passwordResetUrl,
            String newPasswordUrl) {
    }
}
