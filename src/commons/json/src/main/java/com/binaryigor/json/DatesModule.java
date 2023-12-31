package com.binaryigor.json;

import com.fasterxml.jackson.databind.module.SimpleModule;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

public class DatesModule extends SimpleModule {

    public DatesModule() {
        addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer());
        addSerializer(LocalDateTime.class, new LocalDateTimeSerializer());

        addDeserializer(ZonedDateTime.class, new ZonedDateTimeDeserializer());

        addDeserializer(Instant.class, new InstantDeserializer());
        addSerializer(Instant.class, new InstantSerializer());

        addDeserializer(LocalDate.class, new LocalDateDeserializer());
        addSerializer(LocalDate.class, new LocalDateSerializer());
    }
}
