package com.binaryigor.main.user.auth.core.repository;

import com.binaryigor.main.user.auth.core.model.SecondFactorAuthentication;

import java.util.Optional;

public interface SecondFactorAuthenticationRepository {

    void save(SecondFactorAuthentication secondFactorAuthentication);

    Optional<SecondFactorAuthentication> ofEmail(String email);
}
