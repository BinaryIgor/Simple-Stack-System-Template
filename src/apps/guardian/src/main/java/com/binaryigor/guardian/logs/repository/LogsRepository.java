package com.binaryigor.guardian.logs.repository;

import com.binaryigor.guardian.logs.model.LogRecord;

import java.util.List;

public interface LogsRepository {

    void store(List<LogRecord> logs);

    void clear();
}
