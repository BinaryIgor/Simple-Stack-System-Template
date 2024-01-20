package com.binaryigor.guardian.alerts;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/alerts")
public class AlertsController {

    private static final Logger log = LoggerFactory.getLogger(AlertsController.class);


    @PostMapping
    public void add(@RequestBody PrometheusAlerts alerts) {
        log.info("Received {} prom alerts", alerts.alerts().size());
        alerts.alerts().forEach(System.out::println);
        System.out.println();
    }
}
