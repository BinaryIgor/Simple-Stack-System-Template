package com.binaryigor.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;

public class InstantDeserializer extends StdDeserializer<Instant> {

    public InstantDeserializer() {
        super(Instant.class);
    }

    @Override
    public Instant deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        return fromString(p.getText());
    }

    public static Instant fromString(String string) {
        try {
            return tryAsDateTime(string)
                    .orElseGet(() -> asTimestamp(string));
        } catch (Exception e) {
            throw new RuntimeException(
                    "Unix timestamp or datetime in ISO UTC format is required. %s meets neither of these requirements"
                            .formatted(string));
        }
    }

    private static Optional<Instant> tryAsDateTime(String string) {
        try {
            return Optional.of(Instant.parse(string));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private static Instant asTimestamp(String string) {
        var timestamp = Long.parseLong(string);
        return Instant.ofEpochMilli(timestamp);
    }
}
