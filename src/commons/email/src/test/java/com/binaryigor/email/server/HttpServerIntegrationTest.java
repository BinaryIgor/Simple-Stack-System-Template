package com.binaryigor.email.server;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.*;

@Tag("integration")
public abstract class HttpServerIntegrationTest {

    protected final static WireMockServer HTTP_SERVER = new WireMockServer(WireMockConfiguration.wireMockConfig()
            .dynamicPort());

    @BeforeAll
    static void startHttpServer() {
        HTTP_SERVER.start();
    }

    @AfterAll
    static void stopHttpServer() {
        HTTP_SERVER.stop();
    }

    @BeforeEach
    void beforeEach() {
        setup();
    }

    protected void setup() {

    }

    @AfterEach
    void afterEach() {
        HTTP_SERVER.resetAll();
    }
}
