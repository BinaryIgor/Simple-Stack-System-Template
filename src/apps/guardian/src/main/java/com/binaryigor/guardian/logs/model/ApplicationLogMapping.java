package com.binaryigor.guardian.logs.model;

import java.util.List;

public record ApplicationLogMapping(List<String> supportedApplicationsKeywords,
                                    LogMapping mapping) {
}
