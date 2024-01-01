package com.binaryigor.email.model;

import java.util.Map;

public record Email(EmailAddress from,
                    EmailAddress to,
                    String subject,
                    String htmlMessage,
                    String textMessage,
                    String tag,
                    Map<String, String> metadata) {

}
