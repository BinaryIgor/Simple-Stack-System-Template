package com.binaryigor.guardian;

import com.binaryigor.test.TestClock;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.EventListener;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.File;

@Tag("integration")
@ActiveProfiles("integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = IntegrationTest.TestConfig.class)
public abstract class IntegrationTest {

    @TempDir(cleanup = CleanupMode.NEVER)
    protected static File logsRoot;
    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    protected TestClock clock;


    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("logs-storage.file-path", () -> logsRoot.getAbsolutePath());
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        ServerPortListener serverPortListener() {
            return new ServerPortListener();
        }


        @Bean
        @Primary
        public TestClock testClock() {
            return new TestClock();
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
