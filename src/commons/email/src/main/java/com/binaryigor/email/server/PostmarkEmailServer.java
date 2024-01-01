package com.binaryigor.email.server;

import com.binaryigor.email.model.Email;
import com.binaryigor.email.model.EmailAddress;
import com.binaryigor.json.JsonMapper;
import com.binaryigor.tools.CollectionsExtension;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class PostmarkEmailServer implements EmailServer {

    private static final Logger log = LoggerFactory.getLogger(PostmarkEmailServer.class);
    private static final List<String> NEEDED_QUOTES_NAME_CHARACTERS = List.of("<", ">", ",");
    private final HttpClient httpClient;
    private final String postmarkUrl;
    private final String postmarkToken;
    private final int batchSize;
    private final String messageStream;
    private final Retry retry;

    public PostmarkEmailServer(HttpClient httpClient,
                               String postmarkUrl,
                               String postmarkToken,
                               int batchSize,
                               String messageStream,
                               Retry retry) {
        this.httpClient = httpClient;
        this.postmarkUrl = postmarkUrl;
        this.postmarkToken = postmarkToken;
        this.batchSize = batchSize;
        this.messageStream = messageStream;
        this.retry = retry;
    }

    public PostmarkEmailServer(String postmarkUrl, String postmarkToken, int batchSize, String messageStream) {
        this(HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(5))
                        .build(),
                postmarkUrl,
                postmarkToken,
                batchSize,
                messageStream,
                Retry.of("postmark-request", RetryConfig.custom()
                        .maxAttempts(5)
                        .intervalFunction(IntervalFunction.ofExponentialBackoff(Duration.ofMillis(500), 2))
                        .build()));
    }

    public PostmarkEmailServer(String postmarkToken, String messageStream) {
        this("https://api.postmarkapp.com", postmarkToken, 50, messageStream);
    }

    public PostmarkEmailServer(String postmarkToken) {
        this(postmarkToken, "outbound");
    }

    @Override
    public void send(Email email) {
        try {
            var body = JsonMapper.json(PostmarkEmail.fromEmail(email, messageStream));
            sendRequest("email", body);
        } catch (Exception e) {
            log.error("Problem while sending email from {} to {}...", email.from(), email.to(), e);
            throw new EmailException(e);
        }
    }

    private void sendRequest(String endpoint, String body) throws Exception {
        var request = HttpRequest.newBuilder()
                .uri(new URI(postmarkUrl + "/" + endpoint))
                .header("Content-Type", "application/json")
                .header("X-Postmark-Server-Token", postmarkToken)
                .timeout(Duration.ofSeconds(5))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        Retry.decorateCheckedRunnable(retry, () -> {
                    var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                    if (response.statusCode() < 200 || response.statusCode() >= 500) {
                        throw new RuntimeException("Problem while sending email to postmark. Code: %d, body: %s"
                                .formatted(response.statusCode(), response.body()));
                    }
                })
                .unchecked()
                .run();
    }

    @Override
    public void sendBatch(Collection<Email> emails) {
        if (emails.isEmpty()) {
            return;
        }

        var postmarkEmails = emails.stream().map(e -> PostmarkEmail.fromEmail(e, messageStream)).toList();

        for (var batch : CollectionsExtension.toBuckets(postmarkEmails, batchSize)) {
            try {
                var body = JsonMapper.json(batch);
                sendRequest("email/batch", body);
            } catch (Exception e) {
                var addresses = batch.stream().map(PostmarkEmail::to).toList();
                log.error("Problem while sending emails to {}...", addresses, e);
                throw new EmailException(e);
            }
        }
    }

    record PostmarkEmail(String from,
                         String to,
                         String subject,
                         String htmlBody,
                         String textBody,
                         String tag,
                         Map<String, String> metadata,
                         String messageStream) {

        public static PostmarkEmail fromEmail(Email email, String messageStream) {
            return new PostmarkEmail(formattedEmailAddress(email.from()),
                    formattedEmailAddress(email.to()),
                    email.subject(),
                    email.htmlMessage(),
                    email.textMessage(),
                    email.tag(),
                    email.metadata(),
                    messageStream);
        }

        private static String formattedEmailAddress(EmailAddress emailAddress) {
            if (emailAddress.name() == null) {
                return emailAddress.email();
            }

            var name = emailAddress.name();
            for (var q : NEEDED_QUOTES_NAME_CHARACTERS) {
                if (name.contains(q)) {
                    name = "\"%s\"".formatted(name);
                    break;
                }
            }

            return "%s <%s>".formatted(name, emailAddress.email());
        }
    }

}
