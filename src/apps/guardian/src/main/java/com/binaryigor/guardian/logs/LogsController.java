package com.binaryigor.guardian.logs;

import com.binaryigor.guardian.logs.model.LogData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/logs")
public class LogsController {

    private static final Logger log = LoggerFactory.getLogger(LogsController.class);
    private final LogsService service;

    public LogsController(LogsService service) {
        this.service = service;
    }

    @PostMapping
    void add(@RequestBody ContainersLogs containersLogs) {
        log.info("Received {} logs from {}", containersLogs.logs().size(), containersLogs.machine());
        var logs = toLogsData(containersLogs);
        service.add(logs);
    }

    public List<LogData> toLogsData(ContainersLogs logs) {
        return logs.logs().stream()
                .map(l -> {
                    //TODO: do we care about from and to timestamps?
                    return new LogData(logs.machine(), l.containerName(), l.logs());
                })
                .toList();
    }
}
