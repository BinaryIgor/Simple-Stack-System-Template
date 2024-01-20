package com.binaryigor.guardian.logs;

import java.util.List;

public record ContainersLogs(String machine,
                             List<ContainerLogs> logs) {
}
