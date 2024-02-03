package com.binaryigor.guardian.common;

import java.time.Instant;

public class Metrics {
    public static final String APPLICATION_LABEL = "application";
    public static final String MACHINE_LABEL = "machine";

    public static String[] applicationLabels(String machine, String application) {
        return new String[]{MACHINE_LABEL, machine, APPLICATION_LABEL, application};
    }

    public static String fullName(String metric) {
        return "%s_%s".formatted("monitoring", metric);
    }

    public static double secondsTimestamp(Instant timestamp) {
        return secondsTimestamp(timestamp.toEpochMilli());
    }

    public static double secondsTimestamp(long timestamp) {
        return timestamp / 1000.0;
    }

}
