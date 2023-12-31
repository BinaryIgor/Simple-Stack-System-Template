package com.binaryigor.json;

import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class JsonMapperTest {

    @Test
    void shouldFailToDeserializeGivenImmutableNonRecordObject() {
        var nonRecord = new ImmutableNonRecord(22, "name");

        var json = JsonMapper.json(nonRecord);

        Assertions.assertThatThrownBy(() -> JsonMapper.object(json, ImmutableNonRecord.class))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void shouldSerializeAndDeserializeObject() {
        var dataObject = new DataObject(1, "Igor", true);

        var json = JsonMapper.json(dataObject);

        Assertions.assertThat(JsonMapper.object(json, DataObject.class)).isEqualTo(dataObject);
    }

    @Test
    void shouldSerializeAndDeserializeComplexObject() {
        var complexObject = new ComplexObject(1,
                "Some name",
                List.of("A", "b", "c"),
                Set.of(1, 2, 3),
                Map.of("key", 44));

        var json = JsonMapper.json(complexObject);

        Assertions.assertThat(JsonMapper.object(json, ComplexObject.class))
                .isEqualTo(complexObject);
    }

    @Test
    void shouldSerializeAndDeserializeSingleFieldObject() {
        var scalar = new Scalar(2);

        var json = JsonMapper.json(scalar);

        Assertions.assertThat(JsonMapper.object(json, Scalar.class)).isEqualTo(scalar);
    }

    @Test
    void shouldSerializeAndDeserializeObjects() {
        var objects = List.of(new Scalar(1), new Scalar(2));

        var json = JsonMapper.json(objects);

        Assertions.assertThat(JsonMapper.objects(json, Scalar.class)).isEqualTo(objects);
    }

    @Test
    void shouldIgnoreUnknownPropertiesOfObjectWhileSerializing() {
        var json = """
                {
                    "value": 2,
                    "name": "Secret",
                    "valueX": 3
                }
                """;

        Assertions.assertThat(JsonMapper.object(json, Scalar.class))
                .isEqualTo(new Scalar(2));
    }

    @Test
    void shouldThrowExceptionGivenJsonWithLackingProperties() {
        var json = """
                {
                    "id": 1,
                    "name": "Name"
                }
                """;

        Assertions.assertThatThrownBy(() -> JsonMapper.object(json, ComplexObject.class))
                .hasRootCauseExactlyInstanceOf(MismatchedInputException.class);
    }

    @Test
    void shouldSerializeAndDeserializeEnum() {
        var data = new JsonObject(Json.SERIALIZE);
        var json = JsonMapper.json(data);

        Assertions.assertThat(JsonMapper.object(json, JsonObject.class)).isEqualTo(data);
    }

    @Test
    void shouldSerializeAndDeserializeLocalDate() {
        var date = LocalDate.now();
        var dateJson = dateString("\"" + DateTimeFormatter.ISO_DATE.format(date) + "\"");

        var expectedDateObject = new LocalDateObject(date);
        var dateObject = JsonMapper.object(dateJson, LocalDateObject.class);

        Assertions.assertThat(dateObject).isEqualTo(expectedDateObject);
        Assertions.assertThat(JsonMapper.json(dateObject)).isEqualTo(dateJson);
    }

    @Test
    void shouldSerializeAndDeserializeLocalDateTime() {
        var date = LocalDateTime.now().withSecond(0).withNano(0);
        var dateTimeJson = dateString(date.toInstant(ZoneOffset.UTC).toEpochMilli());

        var expectedDateTimeObject = new LocalDateTimeObject(date);
        var dateTimeObject = JsonMapper.object(dateTimeJson, LocalDateTimeObject.class);

        Assertions.assertThat(dateTimeObject).isEqualTo(expectedDateTimeObject);
        Assertions.assertThat(JsonMapper.json(dateTimeObject)).isEqualTo(dateTimeJson);
    }

    @Test
    void shouldSerializeAndDeserializeRecordWithMethods() {
        var record = new RecordWithMethods(22, "Some name", false);

        var json = JsonMapper.json(record);

        var parsedRecord = JsonMapper.object(json, RecordWithMethods.class);

        Assertions.assertThat(parsedRecord).isEqualTo(record);
    }

    @Test
    void shouldSerializeAndDeserializeInstant() {
        var instant = Instant.now();
        var instantObject = new InstantObject(instant);

        var json = JsonMapper.json(instantObject);

        Assertions.assertThat(instantObject)
                .isEqualTo(JsonMapper.object(json, InstantObject.class));
    }

    @Test
    void shouldDeserializeInstantFromTimestamp() {
        var instant = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        var instantObject = new InstantObject(instant);

        var json = """
                {
                  "instant": %d
                }
                """.formatted(instant.toEpochMilli());

        Assertions.assertThat(instantObject)
                .isEqualTo(JsonMapper.object(json, InstantObject.class));
    }

    private String dateString(Object date) {
        return """
                {"date":%s}""".formatted(date);
    }

    private enum Json {
        SERIALIZE
    }

    private record DataObject(long id, String name, boolean success) {
    }

    private record Scalar(int value) {
    }

    private record JsonObject(Json operation) {
    }

    private record LocalDateObject(LocalDate date) {
    }

    private record LocalDateTimeObject(LocalDateTime date) {
    }

    private record InstantObject(Instant instant) {
    }

    private record ComplexObject(long id,
                                 String name,
                                 List<String> languages,
                                 Set<Integer> points,
                                 Map<String, Integer> keysPoints) {
    }

    private record RecordWithMethods(long id, String name, boolean flag) {

        public static RecordWithMethods ofId(long id, boolean flag) {
            return new RecordWithMethods(id, null, flag);
        }

        public long reversedId() {
            return -id;
        }

        public Optional<String> nameOptional() {
            return Optional.ofNullable(name);
        }

        public boolean isEmpty() {
            return nameOptional().isEmpty();
        }

        public boolean empty() {
            return nameOptional().isEmpty();
        }
    }

    //NonRecord intentionally
    private static class ImmutableNonRecord {
        private final long id;
        private final String name;

        public ImmutableNonRecord(long id, String name) {
            this.id = id;
            this.name = name;
        }

        public long id() {
            return id;
        }

        public String name() {
            return name;
        }
    }

}
