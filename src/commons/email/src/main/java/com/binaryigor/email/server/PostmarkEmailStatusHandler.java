package com.binaryigor.email.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;

public class PostmarkEmailStatusHandler {

    public static final String RECORD_TYPE = "RecordType";
    public static final String BOUNCE = "Bounce";
    public static final String DELIVERY = "Delivery";
    private static final Logger log = LoggerFactory.getLogger(PostmarkEmailStatusHandler.class);
    private final Actions actions;

    public PostmarkEmailStatusHandler(Actions actions) {
        this.actions = actions;
    }

    public void handle(Map<String, Object> statusData) {
        var recordType = statusData.get(RECORD_TYPE);
        if (recordType == null) {
            log.warn("Invalid email status data, no {}: {}", RECORD_TYPE, statusData);
            return;
        }

        var metadata = extractMetadata(statusData);

        if (recordType.equals(BOUNCE)) {
            log.error("We got email bounce: {}", statusData);
            actions.onBounce(metadata);
        } else {
            log.info("We got email status: {}", statusData);
            if (recordType.equals(DELIVERY)) {
                actions.onDelivery(metadata);
            }
        }
    }

    private Map<String, String> extractMetadata(Map<String, Object> statusData) {
        try {
            return Optional.ofNullable(statusData.get("Metadata"))
                    .map(m -> (Map<String, String>) m)
                    .orElse(Map.of());
        } catch (Exception e) {
            log.error("Problems while extracting metadata from email data: {}", statusData, e);
            return Map.of();
        }
    }

    public interface Actions {
        void onBounce(Map<String, String> emailMetadata);

        void onDelivery(Map<String, String> emailMetadata);
    }
}
