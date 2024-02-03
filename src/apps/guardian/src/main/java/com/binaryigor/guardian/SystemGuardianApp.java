package com.binaryigor.guardian;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.binaryigor.guardian")
public class SystemGuardianApp {
    public static void main(String[] args) {
        SpringApplication.run(SystemGuardianApp.class, args);
    }
}
