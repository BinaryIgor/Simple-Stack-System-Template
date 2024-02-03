package com.binaryigor.guardian.logs;

import com.binaryigor.guardian.logs.model.*;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

public class LogConverterTest {

    private static final String APPLICATION_MESSAGE_TO_SWALLOW = "app-XXX";
    private static final String DEFAULT_MESSAGE_TO_SWALLOW = "XXX";
    private LogsConverter converter;

    @BeforeEach
    void setup() {
        var applicationLogMappings = List.of(
                new ApplicationLogMapping(List.of("postgres", "pos"),
                        new LogMapping(List.of("FATAL"),
                                List.of("PANIC"),
                                List.of(APPLICATION_MESSAGE_TO_SWALLOW))));

        converter = new LogsConverter(applicationLogMappings,
                new LogMapping(List.of("WARN", "warning"),
                        List.of("ERROR", "error"),
                        List.of(DEFAULT_MESSAGE_TO_SWALLOW)));
    }

    @ParameterizedTest
    @MethodSource("testCases")
    void shouldConvertLogsBasedOnConfigureMappings(LogData toConvert,
                                                   LogRecord converted) {
        Assertions.assertThat(converter.converted(toConvert))
                .isEqualTo(converted);
    }

    static Stream<Arguments> testCases() {
        var appInfo = new LogData("some-machine", "some-postgres", "ordinary log, almost fatal");
        var appWarning = new LogData("some-another-machine", "some-pos", "FATAL failure");
        var appError = new LogData("some-machine-3", "postgres", "PANIC [run!!]");
        var appErrorToSwallow = new LogData("machine", "post",
                "%s PANIC, but operating as usual!".formatted(APPLICATION_MESSAGE_TO_SWALLOW));

        var defaultInfo = new LogData("machine", "business-app",
                "[2022-12-12T09:00:00] Some log on an INFO lvl");
        var defaultWarningI = new LogData("machine-2", "business-app",
                "[2022-12-12T09:00:00] Some log on an warning lvl");
        var defaultWarningII = new LogData("machine-3", "business-app",
                "[2022-12-12T09:00:00] Some log on an WARN lvl");
        var defaultAlmostWarning = new LogData("machine", "business-app",
                "[2022-12-12T09:00:00] Some log on an almost warn lvl");
        var defaultErrorI = new LogData("machine-4", "custom-app",
                "[ERROR] something has happened!");
        var defaultErrorII = new LogData("machine-4", "custom-app",
                "error something has happened!");
        var defaultAlmostError = new LogData("machine-5", "custom-app",
                "[2022-12-12T09:00:00] Some log on an almost ERRO lvl");
        var defaultErrorToSwallow = new LogData("machine-4", "custom-app",
                "error, but operating as usual %s!".formatted(DEFAULT_MESSAGE_TO_SWALLOW));

        return Stream.of(
                Arguments.of(appInfo, toExpectedLogRecord(appInfo, ApplicationLogLevel.INFO)),
                Arguments.of(appWarning, toExpectedLogRecord(appWarning, ApplicationLogLevel.WARNING)),
                Arguments.of(appError, toExpectedLogRecord(appError, ApplicationLogLevel.ERROR)),
                Arguments.of(appErrorToSwallow, toExpectedLogRecord(appErrorToSwallow, ApplicationLogLevel.INFO)),
                Arguments.of(defaultInfo, toExpectedLogRecord(defaultInfo, ApplicationLogLevel.INFO)),
                Arguments.of(defaultWarningI, toExpectedLogRecord(defaultWarningI, ApplicationLogLevel.WARNING)),
                Arguments.of(defaultWarningII, toExpectedLogRecord(defaultWarningII, ApplicationLogLevel.WARNING)),
                Arguments.of(defaultAlmostWarning,
                        toExpectedLogRecord(defaultAlmostWarning, ApplicationLogLevel.INFO)),
                Arguments.of(defaultErrorI, toExpectedLogRecord(defaultErrorI, ApplicationLogLevel.ERROR)),
                Arguments.of(defaultErrorII, toExpectedLogRecord(defaultErrorII, ApplicationLogLevel.ERROR)),
                Arguments.of(defaultAlmostError, toExpectedLogRecord(defaultAlmostError, ApplicationLogLevel.INFO)),
                Arguments.of(defaultErrorToSwallow,
                        toExpectedLogRecord(defaultErrorToSwallow, ApplicationLogLevel.INFO)));
    }

    private static LogRecord toExpectedLogRecord(LogData data, ApplicationLogLevel level) {
        return new LogRecord(data.machine(), data.application(), level, data.log());
    }
}
