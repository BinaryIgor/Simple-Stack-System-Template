package com.binaryigor.main.user.auth.core;

import com.binaryigor.main._commons.core.FieldValidator;
import com.binaryigor.main.user.auth.core.exception.SecondFactorAuthenticationException;
import com.binaryigor.main.user.auth.core.model.SecondFactorAuthentication;
import com.binaryigor.main.user.auth.core.repository.SecondFactorAuthenticationRepository;
import com.binaryigor.main.user.common.core.UserExceptions;
import com.binaryigor.main.user.common.core.model.EmailUser;
import com.binaryigor.tools.Randoms;

import java.time.Clock;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

//TODO: email sender
public class SecondFactorAuthenticator {

    public static final long CODE_VALIDITY = TimeUnit.MINUTES.toSeconds(5);
    private final SecondFactorAuthenticationRepository secondFactorAuthenticationRepository;
//    private final UserEmailSender emailSender;
    private final Supplier<String> nextCodeGenerator;
    private final Clock clock;

    public SecondFactorAuthenticator(SecondFactorAuthenticationRepository secondFactorAuthenticationRepository,
//                                     UserEmailSender emailSender,
                                     Supplier<String> nextCodeGenerator,
                                     Clock clock) {
        this.secondFactorAuthenticationRepository = secondFactorAuthenticationRepository;
//        this.emailSender = emailSender;
        this.nextCodeGenerator = nextCodeGenerator;
        this.clock = clock;
    }

    public SecondFactorAuthenticator(SecondFactorAuthenticationRepository secondFactorAuthenticationRepository,
//                                     UserEmailSender emailSender,
                                     Clock clock) {
        this(secondFactorAuthenticationRepository, () -> Randoms.hash(9), clock);
    }

    public void sendCode(EmailUser user) {
        var code = nextCodeGenerator.get();
        var now = clock.instant();
        var expiresAt = now.plusSeconds(CODE_VALIDITY);

        secondFactorAuthenticationRepository.save(
                new SecondFactorAuthentication(user.id(), user.email(),
                        code, now, expiresAt));

//        emailSender.sendSecondFactorAuthentication(user, code);
    }

    public void validateCode(String email, String code) {
        if (!FieldValidator.hasAnyContent(email) || !FieldValidator.hasAnyContent(code)) {
            throw new SecondFactorAuthenticationException("Email and code are required");
        }

        var secondFactor = secondFactorAuthenticationRepository.ofEmail(email)
                .orElseThrow(() -> UserExceptions.secondFactorAuthenticationNotFound(email));

        if (clock.instant().isAfter(secondFactor.expiresAt())) {
            throw new SecondFactorAuthenticationException("Code has expired, generate new one");
        }

        var strippedCode = code.strip();
        if (!secondFactor.code().equals(strippedCode)) {
            throw new SecondFactorAuthenticationException("Invalid code");
        }
    }
}
