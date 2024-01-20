package com.binaryigor.guardian.logs;

import java.time.Instant;

public record ContainerLogs(String containerName,
                            Instant fromTimestamp,
                            Instant toTimestamp,
                            String logs) {
}
