package com.binaryigor.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.binaryigor.main")
public class SimpleStackSystemTemplateApp {
    public static void main(String[] args) {
        SpringApplication.run(SimpleStackSystemTemplateApp.class, args);
    }
}
