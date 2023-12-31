package com.binaryigor.json;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

public class LocalDateTimeDeserializerTest {

    @Test
    void shouldReturnProperDateTimeGivenTimestampString() {
        var inst = Instant.now(Clock.systemUTC());

        var expected = LocalDateTime.ofInstant(inst, ZoneOffset.UTC)
                .truncatedTo(ChronoUnit.MILLIS);

        Assertions.assertThat(LocalDateTimeDeserializer.fromString(String.valueOf(inst.toEpochMilli())))
                .isEqualTo(expected);
    }

    @Test
    void shouldReturnProperDateTimeGivenDateTimeString() {
        var datetime = "2022-10-10T12:00:00";

        var expected = LocalDateTime.parse(datetime);

        Assertions.assertThat(LocalDateTimeDeserializer.fromString(datetime))
                .isEqualTo(expected);
    }

    @Test
    void shouldReturnProperDateTimeGivenDateTimeStringWithSubSecondsPartAndZ() {
        var datetime = "2023-01-18T21:07:15.028469114Z";

        var expected = LocalDateTime.of(2023, 1, 18, 21, 7, 15, 28469114);

        Assertions.assertThat(LocalDateTimeDeserializer.fromString(datetime))
                .isEqualTo(expected);
    }

    @Test
    void shouldReturnProperDateTimeGivenDateTimeStringWithTimezone() {
        var datetime = "2022-12-02T12:00:00[Europe/Warsaw]";

        var expected = LocalDateTime.parse("2022-12-02T11:00:00");

        Assertions.assertThat(LocalDateTimeDeserializer.fromString(datetime))
                .isEqualTo(expected);
    }

    @Test
    void shouldReturnProperDateTimeGivenDateTimeStringWithTimezoneAndDelimiterCharacter() {
        var datetime = "2022-06-02T12:00:00[America/Argentina/Buenos_Aires]";

        var expected = LocalDateTime.parse("2022-06-02T15:00:00");

        Assertions.assertThat(LocalDateTimeDeserializer.fromString(datetime))
                .isEqualTo(expected);
    }

}
