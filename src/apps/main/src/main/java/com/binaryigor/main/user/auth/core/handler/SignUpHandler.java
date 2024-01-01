package com.binaryigor.main.user.auth.core.handler;

import com.binaryigor.main._common.core.Emails;
import com.binaryigor.main.user.auth.core.model.SignUpCommand;
import com.binaryigor.main.user.common.core.ActivationTokens;
import com.binaryigor.main.user.common.core.PasswordHasher;
import com.binaryigor.main.user.common.core.UserEmailSender;
import com.binaryigor.main.user.common.core.UserValidator;
import com.binaryigor.main.user.common.core.exception.EmailNotReachableException;
import com.binaryigor.main.user.common.core.exception.EmailTakenException;
import com.binaryigor.main.user.common.core.exception.LanguageRequiredException;
import com.binaryigor.main.user.common.core.model.EmailUser;
import com.binaryigor.main.user.common.core.model.User;
import com.binaryigor.main.user.common.core.repository.UserRepository;
import com.binaryigor.types.Transactions;

public class SignUpHandler {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final ActivationTokens activationTokens;
    private final UserEmailSender emailSender;
    private final Transactions transactions;

    public SignUpHandler(UserRepository userRepository,
                         PasswordHasher passwordHasher,
                         ActivationTokens activationTokens,
                         UserEmailSender emailSender,
                         Transactions transactions) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.activationTokens = activationTokens;
        this.emailSender = emailSender;
        this.transactions = transactions;
    }

    public void handle(SignUpCommand command) {
        validateCommand(command);

        if (userRepository.ofEmail(command.email()).isPresent()) {
            throw new EmailTakenException(command.email());
        }

        var toCreateUser = User.newUser(command.id(), command.name(), command.email(), command.language(),
                passwordHasher.hash(command.password()));

        var activationToken = transactions.executeAndReturn(() -> {
            userRepository.create(toCreateUser);
            return activationTokens.saveNewUser(toCreateUser.id()).token();
        });

        emailSender.sendAccountActivation(EmailUser.fromUser(toCreateUser), activationToken);
    }

    private void validateCommand(SignUpCommand command) {
        UserValidator.validateName(command.name());

        var email = command.email();

        UserValidator.validateEmail(email);

        if (!Emails.isReachable(email)) {
            throw new EmailNotReachableException(email);
        }

        if (command.language() == null) {
            throw new LanguageRequiredException();
        }

        UserValidator.validatePassword(command.password());
    }
}
