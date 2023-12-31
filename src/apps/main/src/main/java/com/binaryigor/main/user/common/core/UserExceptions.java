package com.binaryigor.main.user.common.core;

import com.binaryigor.main._commons.core.exception.NotFoundException;
import com.binaryigor.main.user.common.core.model.ActivationTokenId;

import java.util.UUID;

public class UserExceptions {

    public static NotFoundException activationTokenNotFound(ActivationTokenId id) {
        return NotFoundException.ofId("ActivationToken", id);
    }

    public static NotFoundException userOfEmailNotFound(String email) {
        return new NotFoundException("User of %s email doesn't exist".formatted(email));
    }

    public static NotFoundException userOfIdNotFound(UUID id) {
        return new NotFoundException("User of %s id doesn't exist".formatted(id));
    }

    public static NotFoundException secondFactorAuthenticationNotFound(String email) {
        return new NotFoundException("User of %s email has skipped second factor authentication".formatted(email));
    }
}
