package com.binaryigor.main;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.test.context.ActiveProfiles;

import java.util.TimeZone;

@Tag("integration")
@ActiveProfiles(value = {"integration"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = IntegrationTest.TestConfig.class,
        properties = {"ENV=integration"})
public abstract class IntegrationTest {

//    protected static final CustomPostgreSQLContainer POSTGRES = CustomPostgreSQLContainer.instance();

    static {
        //Prevent strange behavior during daylight saving time
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @Autowired
    protected TestRestTemplate restTemplate;

    protected void afterSetup() {

    }

    @AfterEach
    protected void tearDown() {
//        POSTGRES.clearDb();
//        transactionListener.clear();
        afterTearDown();
    }

    protected void afterTearDown() {

    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        ServerPortListener serverPortListener() {
            return new ServerPortListener();
        }

    }

    static class ServerPortListener {
        private int port;

        public int port() {
            return port;
        }

        @EventListener
        public void onApplicationEvent(ServletWebServerInitializedEvent event) {
            port = event.getWebServer().getPort();
        }
    }
}
