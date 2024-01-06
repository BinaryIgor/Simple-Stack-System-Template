package com.binaryigor.main.user.auth.app;

import com.binaryigor.main.IntegrationTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

public class UserAuthControllerTest extends IntegrationTest {

    @Test
    void shouldReturnSignInPage() {
        var response = restTemplate.getForEntity("/sign-in", String.class);

        Assertions.assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);

        Assertions.assertThat(response.getBody())
                .contains("form-container")
                .contains("name")
                .contains("email")
                .contains("/sign-in");
    }
}
