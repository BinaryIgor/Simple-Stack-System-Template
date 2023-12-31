package com.binaryigor.main.user.auth.core.handler;

import com.binaryigor.main._contract.AuthClient;
import com.binaryigor.main._contract.model.UserState;
import com.binaryigor.main.user.auth.core.SecondFactorAuthenticator;
import com.binaryigor.main.user.auth.core.model.CurrentUserData;
import com.binaryigor.main.user.auth.core.model.SignInFirstStepCommand;
import com.binaryigor.main.user.auth.core.model.SignedInUser;
import com.binaryigor.main.user.auth.core.model.SignedInUserStep;
import com.binaryigor.main.user.common.core.PasswordHasher;
import com.binaryigor.main.user.common.core.UserExceptions;
import com.binaryigor.main.user.common.core.UserValidator;
import com.binaryigor.main.user.common.core.exception.NotMatchedPasswordException;
import com.binaryigor.main.user.common.core.exception.UserNotActivatedException;
import com.binaryigor.main.user.common.core.model.EmailUser;
import com.binaryigor.main.user.common.core.model.User;
import com.binaryigor.main.user.common.core.repository.UserRepository;

public class SignInFirstStepHandler {

    private final AuthClient authClient;
    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final SecondFactorAuthenticator secondFactorAuthenticator;

    public SignInFirstStepHandler(AuthClient authClient,
                                  UserRepository userRepository,
                                  PasswordHasher passwordHasher,
                                  SecondFactorAuthenticator secondFactorAuthenticator) {
        this.authClient = authClient;
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.secondFactorAuthenticator = secondFactorAuthenticator;
    }

    public SignedInUserStep handle(SignInFirstStepCommand command) {
        validateCommand(command);

        var user = validatedUser(command.email(), command.password());
        if (user.secondFactorAuth()) {
            secondFactorAuthenticator.sendCode(EmailUser.fromUser(user));
            return SignedInUserStep.firstStep();
        }

        var signedInUser = new SignedInUser(CurrentUserData.fromUser(user), authClient.ofUser(user.id()));

        return SignedInUserStep.onlyStep(signedInUser);
    }

    private void validateCommand(SignInFirstStepCommand command) {
        UserValidator.validateEmail(command.email());
        UserValidator.validatePassword(command.password());
    }

    private User validatedUser(String email, String password) {
        var user = userRepository.ofEmail(email)
                .orElseThrow(() -> UserExceptions.userOfEmailNotFound(email));

        if (!passwordHasher.matches(password, user.password())) {
            throw new NotMatchedPasswordException();
        }

        if (user.state().isAtLeast(UserState.ACTIVATED)) {
            return user;
        }

        throw new UserNotActivatedException();
    }
}
