package com.binaryigor.guardian.logs;

import com.binaryigor.guardian.logs.repository.FileLogsRepository;
import com.binaryigor.guardian.logs.repository.LogsRepository;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
@EnableConfigurationProperties({LogsMappingsConfig.class, LogsStorageConfig.class})
public class LogsConfig {

    @Bean
    public LogsConverter logsConverter(LogsMappingsConfig config) {
        return new LogsConverter(config.applications(), config.defaultMapping());
    }

    @Bean
    public LogsRepository logsRepository(LogsStorageConfig config, Clock clock) {
        return new FileLogsRepository(clock, config.filePath(), config.maxFileSize(), config.maxFiles());
    }

    @Bean
    public LogsCleaner logsCleaner(LogsRepository logsRepository) {
        return new LogsCleaner(logsRepository);
    }

    @Bean
    public LogsService logsService(LogsConverter logsConverter,
                                   LogsRepository logsRepository,
                                   MeterRegistry meterRegistry,
                                   Clock clock) {
        return new LogsService(logsConverter, logsRepository, meterRegistry, clock);
    }
}
