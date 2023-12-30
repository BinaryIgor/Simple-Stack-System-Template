package com.binaryigor.main.auth.app;

import org.springframework.http.HttpMethod;

public record SecurityEndpoint(String url, HttpMethod method) {
}
