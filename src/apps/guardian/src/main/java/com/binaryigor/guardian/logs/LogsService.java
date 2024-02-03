package com.binaryigor.guardian.logs;

import com.binaryigor.guardian.common.Gauges;
import com.binaryigor.guardian.common.Metrics;
import com.binaryigor.guardian.logs.model.ApplicationLogLevel;
import com.binaryigor.guardian.logs.model.LogData;
import com.binaryigor.guardian.logs.repository.LogsRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;

import java.time.Clock;
import java.util.List;

public class LogsService {

    static final String APPLICATION_LOGS_ERRORS_TOTAL = Metrics.fullName("application_logs_errors_total");
    static final String APPLICATION_LOGS_WARNINGS_TOTAL = Metrics.fullName("application_logs_warnings_total");
    static final String APPLICATION_LOGS_ERROR_TIMESTAMP = Metrics.fullName("application_logs_error_timestamp_seconds");
    static final String APPLICATION_LOGS_WARNING_TIMESTAMP =
            Metrics.fullName("application_logs_warning_timestamp_seconds");
    private final LogsConverter logsConverter;
    private final LogsRepository logsRepository;
    private final Counter logsErrorsCounter;
    private final Counter logsWarningsCounter;
    private final Gauges gauges;
    private final Clock clock;

    public LogsService(LogsConverter logsConverter,
                       LogsRepository logsRepository,
                       MeterRegistry meterRegistry,
                       Clock clock) {
        this.logsConverter = logsConverter;
        this.logsRepository = logsRepository;

        logsErrorsCounter = meterRegistry.counter(APPLICATION_LOGS_ERRORS_TOTAL);
        logsWarningsCounter = meterRegistry.counter(APPLICATION_LOGS_WARNINGS_TOTAL);

        logsErrorsCounter.increment(0);
        logsWarningsCounter.increment(0);

        gauges = new Gauges(meterRegistry);

        this.clock = clock;
    }

    public void add(List<LogData> logs) {
        var records = logs.stream()
                .map(l -> {
                    var r = logsConverter.converted(l);
                    setLogTimestampMetric(r.machine(), r.application(), r.level());
                    return r;
                })
                .toList();

        logsRepository.store(records);
    }

    private void setLogTimestampMetric(String machine,
                                       String application,
                                       ApplicationLogLevel logLevel) {
        if (logLevel == ApplicationLogLevel.ERROR) {
            logsErrorsCounter.increment();
            setLogTimestampMetric(APPLICATION_LOGS_ERROR_TIMESTAMP,
                    machine, application);
        } else if (logLevel == ApplicationLogLevel.WARNING) {
            logsWarningsCounter.increment();
            setLogTimestampMetric(APPLICATION_LOGS_WARNING_TIMESTAMP,
                    machine, application);
        }
    }

    private void setLogTimestampMetric(String metricName,
                                       String machine,
                                       String application) {
        var tags = Tags.of(Metrics.applicationLabels(machine, application));
        gauges.updateValue(metricName, tags,
                Metrics.secondsTimestamp(clock.instant()));
    }
}
