package com.binaryigor.guardian.test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class TestMetrics {

    private static final Pattern NO_LABELS_METRIC_PATTERN = Pattern.compile("(.+?) (.+?)");
    private static final Pattern WITH_LABELS_METRIC_PATTERN = Pattern.compile("(.+?)\\{(.+?)}(.+)");

    public static String expectedMetric(String metric, List<String> labelsValues, String value) {
        if (labelsValues.isEmpty()) {
            return "%s %s".formatted(metric, value);
        }

        if (labelsValues.size() % 2 != 0) {
            throw new RuntimeException("Odd size of labelsValues, it has to be even!");
        }

        var labelsValuesBuilder = new StringBuilder();
        for (int i = 0; i < labelsValues.size(); i += 2) {
            var lKey = labelsValues.get(i);
            var lValue = labelsValues.get(i + 1);
            labelsValuesBuilder.append(lKey)
                    .append("=")
                    .append("\"").append(lValue).append("\"")
                    .append(",");
        }

        return "%s{%s} %s".formatted(metric, labelsValuesBuilder.toString(), value);
    }

    public static List<TestMetric> parseMetrics(String metrics) {
        return Arrays.stream(metrics.split("\n"))
                .filter(m -> !m.startsWith("#"))
                .map(TestMetrics::parseMetric)
                .toList();
    }

    public static TestMetric parseMetric(String metric) {
        var strippedMetric = metric.strip();

        var withLabelsMatcher = WITH_LABELS_METRIC_PATTERN.matcher(strippedMetric);
        if (withLabelsMatcher.matches()) {
            var metricName = withLabelsMatcher.group(1);
            var labels = parseMetricLabels(withLabelsMatcher.group(2));
            var value = withLabelsMatcher.group(3);

            return new TestMetric(metricName.strip(), labels, value.strip());
        }

        var noLabelsMatcher = NO_LABELS_METRIC_PATTERN.matcher(strippedMetric);

        if (noLabelsMatcher.matches()) {
            return new TestMetric(noLabelsMatcher.group(1).strip(), Map.of(), noLabelsMatcher.group(2).strip());
        }

        throw new RuntimeException("Given string: %s is not a valid metric".formatted(metric));
    }

    private static Map<String, String> parseMetricLabels(String labels) {
        var labelsValues = new HashMap<String, String>();

        for (var lvToSplit : labels.split(",")) {
            var lv = lvToSplit.strip().split("=", 2);
            if (lv.length > 1) {
                labelsValues.put(lv[0], lv[1].replace("\"", ""));
            }
        }

        return labelsValues;
    }

    public static TestMetric metric(String name, String value) {
        return metric(name, Map.of(), value);
    }

    public static TestMetric metric(String name, Map<String, String> labels, String value) {
        return new TestMetric(name, labels, value);
    }

}
