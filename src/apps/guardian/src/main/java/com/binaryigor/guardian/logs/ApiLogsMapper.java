package com.binaryigor.guardian.logs;

import com.binaryigor.guardian.logs.model.LogData;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ApiLogsMapper {


    public static List<LogData> fromApiLogs(ContainersLogs logs) {
        return logs.logs().stream()
                .map(l -> {
                    //TODO: do we care about from and to timestamps?
                    return new LogData(logs.machine(), l.containerName(), l.logs());
                })
                .toList();
    }

}
