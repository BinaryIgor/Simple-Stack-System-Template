package com.binaryigor.guardian.test;

import java.util.Map;

public record TestMetric(String name, Map<String, String> labels, String value) {
}
