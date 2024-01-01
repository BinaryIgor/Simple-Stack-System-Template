package com.binaryigor.main.auth.app;

import com.binaryigor.main._common.app.Cookies;
import com.binaryigor.main._common.app.HTMX;
import com.binaryigor.main._common.core.exception.AccessForbiddenException;
import com.binaryigor.main._common.core.exception.InvalidAuthTokenException;
import com.binaryigor.main._common.core.exception.UnauthenticatedException;
import com.binaryigor.main.auth.core.AuthTokenAuthenticator;
import com.binaryigor.main.auth.core.AuthenticationResult;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;

public class SecurityFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(SecurityFilter.class);
    private final AuthTokenAuthenticator authTokenAuthenticator;
    private final SecurityRules securityRules;
    private final Cookies cookies;
    private final Clock clock;
    private final Duration issueNewTokenBeforeExpirationDuration;

    public SecurityFilter(AuthTokenAuthenticator authTokenAuthenticator,
                          SecurityRules securityRules,
                          Cookies cookies,
                          Clock clock,
                          Duration issueNewTokenBeforeExpirationDuration) {
        this.authTokenAuthenticator = authTokenAuthenticator;
        this.securityRules = securityRules;
        this.cookies = cookies;
        this.clock = clock;
        this.issueNewTokenBeforeExpirationDuration = issueNewTokenBeforeExpirationDuration;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        var request = (HttpServletRequest) servletRequest;
        var response = (HttpServletResponse) servletResponse;

        var endpoint = new SecurityEndpoint(request.getRequestURI(),
                HttpMethod.valueOf(request.getMethod()));

        try {
            var token = cookies.tokenValue(request.getCookies());

            var authResult = token.map(authTokenAuthenticator::authenticate);
            authResult.ifPresent(r -> AuthenticatedUserRequestHolder.set(r.user()));

            log.info("Auth result: {}", authResult);

            securityRules.validateAccess(endpoint, authResult.map(AuthenticationResult::user));

            authResult.ifPresent(r -> {
                if (shouldIssueNewToken(r)) {
                    issueNewToken(response, token.get());
                }
            });

            filterChain.doFilter(servletRequest, servletResponse);
        } catch (UnauthenticatedException | InvalidAuthTokenException e) {
            sendExceptionResponse(request, response, 401, e);
        } catch (AccessForbiddenException e) {
            sendExceptionResponse(request, response, 403, e);
        }
    }

    private boolean shouldIssueNewToken(AuthenticationResult result) {
        return Duration.between(clock.instant(), result.expiresAt())
                .compareTo(issueNewTokenBeforeExpirationDuration) <= 0;
    }

    // TODO: better abstraction
    private void issueNewToken(HttpServletResponse response, String currentToken) {
        log.info("Issuing new token...");
        var authToken = authTokenAuthenticator.refresh(currentToken);
        response.addCookie(cookies.token(authToken.value(), authToken.expiresAt()));
    }

    //TODO: sth better with exception
    private void sendExceptionResponse(HttpServletRequest request,
                                       HttpServletResponse response,
                                       int status,
                                       Throwable exception) {
        log.warn("Sending redirect from {} status to {}: {} request", status, request.getMethod(), request.getRequestURI());
        try {
            response.setStatus(302);
            response.setHeader("Location", "/sign-in");
        } catch (Exception e) {
            log.error("Problem while writing response body to HttpServletResponse", e);
        }
    }
}
