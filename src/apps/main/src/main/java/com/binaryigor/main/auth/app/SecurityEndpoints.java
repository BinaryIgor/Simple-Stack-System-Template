package com.binaryigor.main.auth.app;

import com.binaryigor.main._contract.model.UserState;
import org.springframework.http.HttpMethod;

import java.util.List;

public class SecurityEndpoints {

    public static final String ADMIN = "/admin";
    public static final String WEBHOOKS = "/webhooks";
    // This is fine since actuator is running on not-exposed to public port
    public static final List<String> PUBLIC_ENDPOINTS = List.of(WEBHOOKS, "/actuator",
            // TODO: change non-page endpoints to user-auth actually for rate limiting also (mostly?)
            "/user-auth",
            "/sign-in",
            "/sign-up",
            "/after-sign-up",
            "/sign-in-second-step",
            "/activate-account",
            "/reset-password",
            "/after-reset-password",
            "/set-new-password",
            "/after-set-new-password");
    private static final List<String> ASSETS_ENDPOINTS = List.of(".css", ".js");

    public static boolean isPublic(SecurityEndpoint endpoint) {
        // Preflight request
        if (endpoint.method() == HttpMethod.OPTIONS) {
            return true;
        }

        for (var e : PUBLIC_ENDPOINTS) {
            if (endpoint.url().startsWith(e)) {
                return true;
            }
        }

        for (var e : ASSETS_ENDPOINTS) {
            if (endpoint.url().endsWith(e)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isMetricEndpoint(SecurityEndpoint endpoint) {
        return endpoint.url().startsWith("/actuator");
    }

    //TODO: implement if needed
    public static boolean isUserOfStateAllowed(SecurityEndpoint endpoint, UserState state) {
        return true;
    }

    public static boolean isAdmin(SecurityEndpoint endpoint) {
        return endpoint.url().startsWith(ADMIN);
    }
}
