package com.binaryigor.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.regex.Pattern;

public class LocalDateTimeDeserializer extends StdDeserializer<LocalDateTime> {

    private static final Pattern DATE_TIME_TIMEZONE_PATTERN = Pattern.compile("(.*)\\[(.*)]");
    private static final String REDUNDANT_UTC_ZONE = "Z";

    public LocalDateTimeDeserializer() {
        super(LocalDateTime.class);
    }

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        return fromString(p.getText());
    }

    public static LocalDateTime fromString(String dateString) {
        try {
            var dateTimezone = dateAndTimezone(dateString);
            var date = sanitizedDate(dateTimezone.date());
            var timezone = dateTimezone.timezone();

            return tryAsTimestamp(date)
                    .map(t -> LocalDateTime.ofInstant(Instant.ofEpochMilli(t), ZoneOffset.UTC))
                    .orElseGet(() -> dateTimeAtZone(
                            LocalDateTime.parse(date, DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                            timezone));
        } catch (Exception e) {
            throw new RuntimeException(
                    "Unix timestamp or date in ISO UTC format is required (with or without timezone). %s meets neither of these requirements"
                            .formatted(dateString));
        }
    }

    private static DateAndTimezone dateAndTimezone(String dateString) {
        String date;
        ZoneId timezone;

        var matcher = DATE_TIME_TIMEZONE_PATTERN.matcher(dateString);
        if (matcher.matches()) {
            date = matcher.group(1);
            timezone = ZoneId.of(matcher.group(2));
        } else {
            date = dateString;
            timezone = null;
        }

        return new DateAndTimezone(date, timezone);
    }

    private static String sanitizedDate(String date) {
        if (date.endsWith(REDUNDANT_UTC_ZONE)) {
            return date.substring(0, date.length() - REDUNDANT_UTC_ZONE.length());
        }
        return date;
    }

    private static Optional<Long> tryAsTimestamp(String date) {
        try {
            return Optional.of(Long.parseLong(date));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private static LocalDateTime dateTimeAtZone(LocalDateTime dateTime, ZoneId zoneId) {
        if (zoneId == null) {
            return dateTime;
        }
        var zoned = dateTime.atZone(zoneId);
        return LocalDateTime.ofInstant(zoned.toInstant(), ZoneOffset.UTC);
    }

    private record DateAndTimezone(String date, ZoneId timezone) {}
}
