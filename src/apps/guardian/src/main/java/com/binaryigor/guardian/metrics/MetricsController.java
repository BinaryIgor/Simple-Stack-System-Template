package com.binaryigor.guardian.metrics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/metrics")
public class MetricsController {

    private static final Logger log = LoggerFactory.getLogger(MetricsController.class);
    @PostMapping
    void add(@RequestBody ContainersMetrics metrics) {
        log.info("Received {} metrics from {}", metrics.metrics().size(), metrics.machine());
        metrics.metrics().forEach(System.out::println);
        System.out.println();
    }
}
