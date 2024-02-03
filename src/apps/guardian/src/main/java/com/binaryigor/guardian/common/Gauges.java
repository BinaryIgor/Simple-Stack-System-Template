package com.binaryigor.guardian.common;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class Gauges {

    private final Map<String, AtomicReference<Double>> metricsReferences = new ConcurrentHashMap<>();
    private final MeterRegistry registry;
    private final MetricIdSupplier metricIdSupplier;

    public Gauges(MeterRegistry registry, MetricIdSupplier metricIdSupplier) {
        this.registry = registry;
        this.metricIdSupplier = metricIdSupplier;
    }

    public Gauges(MeterRegistry registry) {
        this(registry, (m, t) -> m + ":" + t.stream()
                .map(Tag::getValue)
                .sorted()
                .collect(Collectors.joining()));
    }

    public void updateValue(String metric, Tags tags, double value) {
        var key = metricIdSupplier.get(metric, tags);
        var previousReference = metricsReferences.computeIfAbsent(key,
                k -> new AtomicReference<>());
        previousReference.set(value);

        registry.gauge(metric, tags, previousReference, AtomicReference::get);
    }

    public interface MetricIdSupplier {
        String get(String metric, Tags tags);
    }
}
