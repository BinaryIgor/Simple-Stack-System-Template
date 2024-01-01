package com.binaryigor.email.server;

import com.binaryigor.email.model.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public class ToConsoleEmailServer implements EmailServer {

    private static final Logger log = LoggerFactory.getLogger(ToConsoleEmailServer.class);

    @Override
    public void send(Email email) {
        log.info("Sending email...{}", email);
    }

    @Override
    public void sendBatch(Collection<Email> emails) {
        log.info("Sending emails {}....", emails.size());
        emails.forEach(this::send);
    }
}
