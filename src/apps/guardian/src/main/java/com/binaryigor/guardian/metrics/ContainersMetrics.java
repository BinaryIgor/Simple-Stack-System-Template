package com.binaryigor.guardian.metrics;

import java.util.List;

public record ContainersMetrics(String machine,
                                List<ContainerMetrics> metrics) {
}
