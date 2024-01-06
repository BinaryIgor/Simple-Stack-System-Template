package com.binaryigor.main.user.auth.core.handler;

import com.binaryigor.main.user.auth.core.model.SetNewPasswordCommand;
import com.binaryigor.main.user.common.core.ActivationTokenConsumer;
import com.binaryigor.main.user.common.core.PasswordHasher;
import com.binaryigor.main.user.common.core.UserValidator;
import com.binaryigor.main.user.common.core.exception.DifferentPasswordException;
import com.binaryigor.main.user.common.core.model.ActivationTokenType;
import com.binaryigor.main.user.common.core.repository.UserUpdateRepository;

public class SetNewUserPasswordHandler {

    private final ActivationTokenConsumer activationTokenConsumer;
    private final UserUpdateRepository userUpdateRepository;
    private final PasswordHasher passwordHasher;


    public SetNewUserPasswordHandler(ActivationTokenConsumer activationTokenConsumer,
                                     UserUpdateRepository userUpdateRepository,
                                     PasswordHasher passwordHasher) {
        this.activationTokenConsumer = activationTokenConsumer;
        this.userUpdateRepository = userUpdateRepository;
        this.passwordHasher = passwordHasher;
    }

    public void handle(SetNewPasswordCommand command) {
        UserValidator.validatePassword(command.password());

        if (!command.password().equals(command.repeatedPassword())) {
            throw new DifferentPasswordException();
        }

        activationTokenConsumer.consume(command.token(), ActivationTokenType.PASSWORD_RESET,
                userId -> {
                    var hashedPassword = passwordHasher.hash(command.password());
                    userUpdateRepository.updatePassword(userId, hashedPassword);
                });
    }
}
