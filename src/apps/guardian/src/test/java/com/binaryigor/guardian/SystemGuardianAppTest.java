package com.binaryigor.guardian;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@Tag("integration")
@ActiveProfiles(value = {"integration"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"ENV=integration"})
public class SystemGuardianAppTest {

    @Test
    void shouldStart() {
        System.out.println("App has started!");
    }
}
