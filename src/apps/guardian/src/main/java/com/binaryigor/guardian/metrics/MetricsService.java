package com.binaryigor.guardian.metrics;

import com.binaryigor.guardian.common.Gauges;
import com.binaryigor.guardian.common.Metrics;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import org.springframework.stereotype.Service;

import java.time.Clock;

@Service
public class MetricsService {

    private static final String COLLECTOR_UP_TIMESTAMP = Metrics.fullName("collector_up_timestamp_seconds");
    private static final String APPLICATION_STARTED_AT_TIMESTAMP =
            Metrics.fullName("application_started_at_timestamp_seconds");
    private static final String APPLICATION_UP_TIMESTAMP = Metrics.fullName("application_up_timestamp_seconds");
    private static final String APPLICATION_USED_MEMORY = Metrics.fullName("application_used_memory_bytes");
    private static final String APPLICATION_MAX_MEMORY = Metrics.fullName("application_max_memory_bytes");
    private static final String APPLICATION_CPU_USAGE = Metrics.fullName("application_cpu_usage_percent");
    private final Gauges gauges;
    private final Clock clock;

    public MetricsService(MeterRegistry meterRegistry, Clock clock) {
        this.gauges = new Gauges(meterRegistry);
        this.clock = clock;
    }

    public void add(ContainersMetrics containersMetrics) {
        gauges.updateValue(COLLECTOR_UP_TIMESTAMP, Tags.of(Metrics.MACHINE_LABEL, containersMetrics.machine()),
                Metrics.secondsTimestamp(clock.instant()));

        for (var m : containersMetrics.metrics()) {
            var tags = Tags.of(Metrics.applicationLabels(containersMetrics.machine(), m.containerName()));

            gauges.updateValue(APPLICATION_STARTED_AT_TIMESTAMP, tags, Metrics.secondsTimestamp(m.startedAt()));
            gauges.updateValue(APPLICATION_UP_TIMESTAMP, tags, Metrics.secondsTimestamp(m.timestamp()));
            gauges.updateValue(APPLICATION_USED_MEMORY, tags, m.usedMemory());
            gauges.updateValue(APPLICATION_MAX_MEMORY, tags, m.maxMemory());
            gauges.updateValue(APPLICATION_CPU_USAGE, tags, m.cpuUsage());
        }
    }

}
