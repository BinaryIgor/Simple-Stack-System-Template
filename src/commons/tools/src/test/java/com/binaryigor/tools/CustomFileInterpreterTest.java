package com.binaryigor.tools;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;

public class CustomFileInterpreterTest {

    @Test
    void shouldReturnSectionsOfCustomFile() {
        var file = """
                txt {
                    some file
                }
                outer {
                    inner {
                        sth
                    }
                }
                                
                                
                html{
                    <!DOCTYPE html>
                        <html lang="${language}">
                                    
                        <head>
                            <meta charset="UTF-8">
                        </head>
                    </html>
                }
                                
                variables{
                    1, 2,3, ${var}
                }
                """;

        var expected = new LinkedHashMap<String, String>();
        expected.put("txt", "some file");
        expected.put("html", """
                <!DOCTYPE html>
                    <html lang="${language}">
                                
                    <head>
                        <meta charset="UTF-8">
                    </head>
                </html>""".strip());
        expected.put("outer", """
                inner {
                    sth
                }""".strip());
        expected.put("variables", "1, 2,3, ${var}");

        var actual = CustomFileInterpreter.sections(file);

        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @Test
    void shouldReturnVariablesFromVariablesSection() {
        var section = """
                a:33
                                
                b : 99=
                c: 999: 0xA
                """;

        var expected = new LinkedHashMap<String, String>();
        expected.put("a", "33");
        expected.put("b", "99=");
        expected.put("c", "999: 0xA");

        Assertions.assertThat(CustomFileInterpreter.variablesSection(section))
                .isEqualTo(expected);
    }

    @Test
    void shouldReturnListFromListSection() {
        var section = """
                a, b, c,
                99,X
                """;

        var expected = List.of("a", "b", "c", "99", "X");

        Assertions.assertThat(CustomFileInterpreter.listSection(section))
                .isEqualTo(expected);
    }
}
