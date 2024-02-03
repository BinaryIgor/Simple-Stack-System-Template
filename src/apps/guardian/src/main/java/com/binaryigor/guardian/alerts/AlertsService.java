package com.binaryigor.guardian.alerts;

import com.binaryigor.guardian.common.Gauges;
import com.binaryigor.guardian.common.Metrics;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AlertsService {

    static final String DESCRIPTION = "description";
    static final String ALERTS_TOTAL = Metrics.fullName("alerts_total");
    static final String ALERT_TIMESTAMP = Metrics.fullName("alert_timestamp_seconds");
    private static final String ERROR_REGEX = "error|ERROR|Error";
    private static final String ERROR_REPLACEMENT = ".e.";
    private static final Logger log = LoggerFactory.getLogger(AlertsService.class);
    private final Counter alertsFiringCounter;
    private final Counter alertsResolvedCounter;
    private final Gauges gauges;
    private final Clock clock;

    public AlertsService(MeterRegistry meterRegistry, Clock clock) {
        alertsFiringCounter = meterRegistry.counter(ALERTS_TOTAL, Alerts.STATUS, Alerts.FIRING);
        alertsResolvedCounter = meterRegistry.counter(ALERTS_TOTAL, Alerts.STATUS, Alerts.RESOLVED);

        alertsFiringCounter.increment(0);
        alertsResolvedCounter.increment(0);

        gauges = new Gauges(meterRegistry);

        this.clock = clock;
    }

    public void add(List<Alert> alerts) {
        for (var a : alerts) {
            var alertToLog = a.toString().replaceAll(ERROR_REGEX, ERROR_REPLACEMENT);
            log.info("Alert: {}", alertToLog);
            increaseAlertsTotal(a);
            setAlertTimestamp(a);
        }
    }

    private List<Tag> prepareLabelsTags(Alert alert) {
        return prepareTags("label", alert.labels());
    }

    private List<Tag> prepareAnnotationsTags(Alert alert) {
        return prepareTags("annotation", alert.annotations());
    }

    private List<Tag> prepareTags(String prefix, Map<String, String> data) {
        return data.entrySet().stream()
                .filter(e -> !e.getKey().contains(DESCRIPTION))
                .map(e -> Tag.of("%s_%s".formatted(prefix, e.getKey()), e.getValue()))
                .toList();
    }

    private void increaseAlertsTotal(Alert alert) {
        if (alert.status().equals(Alerts.FIRING)) {
            alertsFiringCounter.increment();
        } else if (alert.status().equals(Alerts.RESOLVED)) {
            alertsResolvedCounter.increment();
        } else {
            log.error("Unknown alert status for alert: {}", alert);
        }
    }

    private void setAlertTimestamp(Alert alert) {
        var tags = new ArrayList<Tag>();
        tags.add(Tag.of(Alerts.STATUS, alert.status()));
        tags.addAll(prepareLabelsTags(alert));
        tags.addAll(prepareAnnotationsTags(alert));

        gauges.updateValue(ALERT_TIMESTAMP, Tags.of(tags),
                Metrics.secondsTimestamp(clock.instant()));
    }
}
