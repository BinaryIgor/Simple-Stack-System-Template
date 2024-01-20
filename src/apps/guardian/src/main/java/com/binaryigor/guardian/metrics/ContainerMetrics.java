package com.binaryigor.guardian.metrics;

import java.time.Instant;

public record ContainerMetrics(String containerName,
                               Instant startedAt,
                               Instant timestamp,
                               long usedMemory,
                               long maxMemory,
                               double cpuUsage) {
}
