package com.binaryigor.guardian.logs;

import com.binaryigor.guardian.logs.model.ApplicationLogMapping;
import com.binaryigor.guardian.logs.model.LogMapping;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collection;

@ConfigurationProperties("logs-mappings")
public record LogsMappingsConfig(Collection<ApplicationLogMapping> applications,
                                 LogMapping defaultMapping) {
}
