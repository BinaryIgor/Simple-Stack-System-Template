package com.binaryigor.guardian.logs;

import com.binaryigor.guardian.IntegrationTest;
import com.binaryigor.guardian.logs.repository.FileLogsRepository;
import com.binaryigor.guardian.test.TestMetric;
import com.binaryigor.guardian.test.TestMetrics;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AutoConfigureObservability
public class LogsControllerTest extends IntegrationTest {

    @BeforeEach
    void setup() {
        clock.setTime(Instant.parse("2022-12-22T19:11:22Z"));
    }

    //TODO: find better way - temp dir needs to be reread into a Spring Context!
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    @Test
    void shouldAddLogsAndUpdatePrometheusMetrics() {
        var testCase = prepareAddLogsTestCase();

        testCase.logsToSend.forEach(logs -> {
            var logsResponse = restTemplate.postForEntity("/logs", logs, Void.class);
            Assertions.assertThat(logsResponse.getStatusCode())
                    .isEqualTo(HttpStatus.OK);
        });

        var metricsResponse = restTemplate.getForEntity("/actuator/prometheus", String.class);
        Assertions.assertThat(metricsResponse.getStatusCode())
                .isEqualTo(HttpStatus.OK);

        var parsedMetrics = TestMetrics.parseMetrics(metricsResponse.getBody());

        Assertions.assertThat(parsedMetrics)
                .containsAll(testCase.expectedMetrics);

        testCase.expectedLogFiles()
                .forEach(p -> Assertions.assertThat(Files.exists(p))
                        .withFailMessage(() -> "%s file doesn't exist!".formatted(p))
                        .isTrue());
    }

    private AddLogsTestCase prepareAddLogsTestCase() {
        var logs = List.of(new LogEntry("anonymous", "some-container", "some-log ERROR"),
                new LogEntry("anonymous", "some-container", "ERROR next"),
                new LogEntry("anonymous-x", "some-container-nginx", "[ERROR] some message"),
                new LogEntry("anonymous", "some-container", "some-log info"),
                new LogEntry("anonymousII", "some-containerII", "some-log WARNING"),
                new LogEntry("anonymousII", "some-containerII", "some-log WARN"));

        var expectedMetrics = List.of(
                TestMetrics.metric("monitoring_application_logs_errors_total", "3.0"),
                TestMetrics.metric("monitoring_application_logs_warnings_total", "2.0"),
                TestMetrics.metric("monitoring_application_logs_error_timestamp_seconds",
                        Map.of("application", "some-container", "machine", "anonymous"),
                        toSecondsTimestampString()),
                TestMetrics.metric("monitoring_application_logs_error_timestamp_seconds",
                        Map.of("application", "some-container-nginx",
                                "machine", "anonymous-x"),
                        toSecondsTimestampString()),
                TestMetrics.metric("monitoring_application_logs_warning_timestamp_seconds",
                        Map.of("application", "some-containerII",
                                "machine", "anonymousII"),
                        toSecondsTimestampString()));

        var logsToSend = toContainersLogs(logs);

        var expectedLogFiles = logs.stream()
                .map(l -> FileLogsRepository.absoluteLogFilePath(logsRoot,
                        l.machineName,
                        l.containerName))
                .toList();

        return new AddLogsTestCase(logsToSend, expectedMetrics, expectedLogFiles);
    }

    private String toSecondsTimestampString() {
        return String.valueOf(clock.instant().toEpochMilli() / 1000.0);
    }

    private List<ContainersLogs> toContainersLogs(List<LogEntry> logEntries) {
        return logEntries.stream()
                .collect(Collectors.groupingBy(e -> e.machineName))
                .entrySet()
                .stream()
                .map(e -> {
                    var m = e.getKey();
                    var logs = e.getValue().stream()
                            //TODO: what about timestamps?
                            .map(c -> new ContainerLogs(c.containerName, null, null, c.log))
                            .toList();

                    return new ContainersLogs(m, logs);
                })
                .toList();
    }

    private record LogEntry(String machineName,
                            String containerName,
                            String log) {

    }

    private record AddLogsTestCase(List<ContainersLogs> logsToSend,
                                   List<TestMetric> expectedMetrics,
                                   List<Path> expectedLogFiles) {
    }
}
