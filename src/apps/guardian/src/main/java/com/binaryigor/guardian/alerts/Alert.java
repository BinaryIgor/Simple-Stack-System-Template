package com.binaryigor.guardian.alerts;

import java.util.Map;

public record Alert(String status,
                    Map<String, String> labels,
                    Map<String, String> annotations) {
}
