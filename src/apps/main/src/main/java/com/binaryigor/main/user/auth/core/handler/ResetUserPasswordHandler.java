package com.binaryigor.main.user.auth.core.handler;

import com.binaryigor.main.user.common.core.ActivationTokens;
import com.binaryigor.main.user.common.core.UserEmailSender;
import com.binaryigor.main.user.common.core.UserExceptions;
import com.binaryigor.main.user.common.core.UserValidator;
import com.binaryigor.main.user.common.core.model.EmailUser;
import com.binaryigor.main.user.common.core.repository.UserRepository;

public class ResetUserPasswordHandler {

    private final UserRepository userRepository;
    private final ActivationTokens activationTokens;
    private final UserEmailSender emailSender;


    public ResetUserPasswordHandler(UserRepository userRepository,
                                    ActivationTokens activationTokens,
                                    UserEmailSender emailSender) {
        this.userRepository = userRepository;
        this.activationTokens = activationTokens;
        this.emailSender = emailSender;
    }

    public void handle(String email) {
        UserValidator.validateEmail(email);

        var user = userRepository.ofEmail(email)
                .orElseThrow(() -> UserExceptions.userOfEmailNotFound(email));

        var resetToken = activationTokens.savePasswordReset(user.id());

        emailSender.sendPasswordReset(EmailUser.fromUser(user), resetToken.token());
    }

}
