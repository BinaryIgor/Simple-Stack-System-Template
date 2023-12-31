package com.binaryigor.main.user.auth.infra;

import com.binaryigor.main.user.auth.core.model.SecondFactorAuthentication;
import com.binaryigor.main.user.auth.core.repository.SecondFactorAuthenticationRepository;

import java.util.Optional;

//TODO: impl
public class InMemorySecondFactorAuthenticationRepository implements SecondFactorAuthenticationRepository {

    @Override
    public void save(SecondFactorAuthentication secondFactorAuthentication) {

    }

    @Override
    public Optional<SecondFactorAuthentication> ofEmail(String email) {
        return Optional.empty();
    }
}
