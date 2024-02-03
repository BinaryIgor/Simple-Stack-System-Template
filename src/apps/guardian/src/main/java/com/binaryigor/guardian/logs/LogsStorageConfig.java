package com.binaryigor.guardian.logs;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "logs-storage")
public record LogsStorageConfig(String filePath,
                                int maxFileSize,
                                int maxFiles) {

}
