package com.binaryigor.guardian.logs;

import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/logs")
public class LogsController {

    private static final Logger log = LoggerFactory.getLogger(LogsController.class);

    @PostMapping
    void add(@RequestBody ContainersLogs logs) {
        log.info("Received {} logs from {}", logs.logs().size(), logs.machine());
        logs.logs().forEach(l -> {
            var toPrintLog = l.logs();
            if (toPrintLog.length() > 50) {
                toPrintLog = toPrintLog.substring(0, 50) + "...";
            }
            System.out.println("Logs of %s container:".formatted(l.containerName()));
            System.out.println(toPrintLog);
        });
        System.out.println();
    }

    @PreDestroy
    public void cleanUp() {
        System.out.println();
        System.out.println("Cleaning up");
        System.out.println();
    }
}
