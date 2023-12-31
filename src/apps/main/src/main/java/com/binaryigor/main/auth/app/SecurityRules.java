package com.binaryigor.main.auth.app;

import com.binaryigor.main._contract.model.AuthenticatedUser;
import com.binaryigor.main._contract.model.UserState;
import com.binaryigor.main._commons.core.exception.AccessForbiddenException;
import com.binaryigor.main._commons.core.exception.UnauthenticatedException;

import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class SecurityRules {

    private final Predicates predicates;

    public SecurityRules(Predicates predicates) {
        this.predicates = predicates;
    }

    public void validateAccess(SecurityEndpoint endpoint,
                               Optional<AuthenticatedUser> user) {
        if (predicates.publicEndpoint.test(endpoint)) {
            return;
        }

        if (user.isPresent()) {
            validateUserHasAccessToEndpoint(endpoint, user.get());
        } else {
            throw new UnauthenticatedException();
        }
    }

    private void validateUserHasAccessToEndpoint(SecurityEndpoint endpoint, AuthenticatedUser user) {
        if (predicates.adminEndpoint().test(endpoint) && !user.isAdmin()) {
            throw new AccessForbiddenException("User is not an admin");
        }
        if (!predicates.isUserOfStateAllowed().test(endpoint, user.state())) {
            throw new AccessForbiddenException(
                    "User of %s state can't access requested resource".formatted(user.state()));
        }
        if (user.banned() && !predicates.isBannedUserAllowed().test(endpoint)) {
            throw new AccessForbiddenException("Banned user is not allowed to access requested resource");
        }
    }

    public record Predicates(Predicate<SecurityEndpoint> publicEndpoint,
                             BiPredicate<SecurityEndpoint, UserState> isUserOfStateAllowed,
                             Predicate<SecurityEndpoint> isBannedUserAllowed,
                             Predicate<SecurityEndpoint> adminEndpoint) {
    }

}
