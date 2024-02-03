package com.binaryigor.guardian.logs;

import com.binaryigor.guardian.logs.model.*;

import java.util.Collection;
import java.util.List;

public class LogsConverter {

    private final Collection<ApplicationLogMapping> applicationLogMappings;
    private final LogMapping defaultLogMapping;

    public LogsConverter(Collection<ApplicationLogMapping> applicationLogMappings,
                         LogMapping defaultLogMapping) {
        this.applicationLogMappings = applicationLogMappings;
        this.defaultLogMapping = defaultLogMapping;
    }

    public LogRecord converted(LogData log) {
        return new LogRecord(log.machine(), log.application(), logLevel(log), log.log());
    }

    private ApplicationLogLevel logLevel(LogData log) {
        var mapping = defaultLogMapping;

        for (var m : applicationLogMappings) {
            if (containsAny(log.application(), m.supportedApplicationsKeywords())) {
                mapping = m.mapping();
                break;
            }
        }

        var logMessage = log.log();

        if (containsAny(logMessage, mapping.errorKeywords())) {
            return containsAny(logMessage, mapping.messagesToSwallow()) ? ApplicationLogLevel.INFO :
                    ApplicationLogLevel.ERROR;
        }
        if (containsAny(logMessage, mapping.warningKeywords())) {
            return containsAny(logMessage, mapping.messagesToSwallow()) ? ApplicationLogLevel.INFO :
                    ApplicationLogLevel.WARNING;
        }

        return ApplicationLogLevel.INFO;
    }

    private boolean containsAny(String string, List<String> keywords) {
        return keywords.stream().anyMatch(string::contains);
    }

}
