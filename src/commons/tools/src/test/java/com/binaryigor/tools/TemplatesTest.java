package com.binaryigor.tools;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class TemplatesTest {

    @Test
    void shouldRenderTemplateWithVariables() {
        var template = """
                Hey ${X}!
                                
                It's been a long time, since we have met with ${y}!
                """;

        var variables = Map.of("X", "Nietzsche",
                "y", "Friedrich");

        var expected = """
                Hey Nietzsche!
                                
                It's been a long time, since we have met with Friedrich!
                """;

        Assertions.assertThat(Templates.rendered(template, variables))
                .isEqualTo(expected);
    }

    @Test
    void shouldRenderTemplateWithComponentsAndVariables() {
        var template = """
                Hey ${x1}!
                                
                #{component1}
                """;

        var variables = Map.of("x1", "A", "x2", "B");
        var components = Map.of("component1", "Embedded service with a variable: ${x2}");

        var expected = """
                Hey A!
                                
                Embedded service with a variable: B
                """;

        Assertions.assertThat(Templates.rendered(template, variables, components))
                .isEqualTo(expected);
    }
}
