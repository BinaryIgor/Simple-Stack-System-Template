package com.binaryigor.tools;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class DataTokensTest {

    @Test
    void shouldCreateDataTokenContainingStringData() {
        var data = "some-string-data";

        var firstToken = DataTokens.containing(data);
        var secondToken = DataTokens.containing(data);

        Assertions.assertThat(firstToken).isNotEqualTo(secondToken);

        Assertions.assertThat(DataTokens.extractedData(firstToken))
                .isEqualTo(data);
        Assertions.assertThat(DataTokens.extractedData(secondToken))
                .isEqualTo(data);
    }

    @Test
    void shouldCreateDataTokenContainingObjectData() {
        var data = new TokenData(99, "some-name-22");

        var firstToken = DataTokens.containing(data);
        var secondToken = DataTokens.containing(data);

        Assertions.assertThat(firstToken).isNotEqualTo(secondToken);

        Assertions.assertThat(DataTokens.extractedData(firstToken, TokenData.class))
                .isEqualTo(data);
        Assertions.assertThat(DataTokens.extractedData(secondToken, TokenData.class))
                .isEqualTo(data);
    }

    @ParameterizedTest
    @MethodSource("invalidDataTokens")
    void shouldThrowExceptionWhileExtractingDataFromInvalidToken(String token) {
        Assertions.assertThatThrownBy(() -> DataTokens.extractedData(token))
                .isInstanceOf(RuntimeException.class);
    }

    static Stream<String> invalidDataTokens() {
        return Stream.of(null, " ", "some-data:9922", Base64Url.asEncoded("some-data"));
    }

    private record TokenData(long id, String name) {
    }
}
