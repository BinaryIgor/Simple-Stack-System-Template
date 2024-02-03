package com.binaryigor.guardian.logs.model;

public record LogRecord(String machine,
                        String application,
                        ApplicationLogLevel level,
                        String log) {
}
