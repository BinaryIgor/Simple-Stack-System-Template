package com.binaryigor.email.server;

import com.binaryigor.email.model.Email;
import com.binaryigor.email.model.EmailAddress;
import com.binaryigor.json.JsonMapper;
import com.binaryigor.test.TestRandom;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

//TODO: fix lacking tests
public class PostmarkEmailServerTest extends HttpServerIntegrationTest {

    private static final int REQUEST_ATTEMPTS = 5;
    private static final int BATCH_SIZE = 2;
    private static final String MESSAGE_STREAM = "outbound";
    private PostmarkEmailServer emailServer;
    private PostmarkStub postmarkStub;
    private String postmarkToken;

    protected void setup() {
        postmarkToken = TestRandom.string();
        emailServer = new PostmarkEmailServer(HTTP_SERVER.baseUrl(), postmarkToken, BATCH_SIZE, MESSAGE_STREAM);
        postmarkStub = new PostmarkStub(HTTP_SERVER);
    }

    @Test
    void shouldSendEmailThroughApi() {
        var testCase = sendEmailTestCase();
        var request = testCase.request;
        var email = testCase.email;

//        postmarkStub.stubEmailRequest(request.body, request.headers);

        emailServer.send(email);

        postmarkStub.verifyEmailRequest(request.body, request.headers);
    }

//    @Test
//    void shouldThrowExceptionAfterRetrying() {
//        var statusCode = TestRandom.inRange(500, 600);
//        var body = """
//                {
//                    "error": "Some error encoded into JSON"
//                }
//                """.trim();
//
//        HTTP_SERVER.expectResponse(statusCode, body);
//
//        Assertions.assertThat(HTTP_SERVER.requestsCount()).isZero();
//
//        Assertions.assertThatThrownBy(() -> emailServer.send(randomEmail()))
//                .isInstanceOf(EmailServer.EmailException.class)
//                .hasMessageContaining(body);
//
//        Assertions.assertThat(HTTP_SERVER.requestsCount())
//                .isEqualTo(REQUEST_ATTEMPTS);
//    }
//
//    @Test
//    void shouldSendEmailBatchThroughApi() {
//        var testCase = sendEmailsTestCase();
//
//        Assertions.assertThat(testCase.requests).hasSizeGreaterThan(1);
//
//        testCase.requests.forEach(r ->
//                HTTP_SERVER.expectations()
//                        .request(r.method, r.url, r.headers, r.body)
//                        .prepare());
//
//        emailServer.sendBatch(testCase.emails);
//    }
//
//    @Test
//    void shouldThrowExceptionAfterRetryingBatch() {
//        var code = TestRandom.inRange(500, 600);
//        var body = """
//                {
//                    "error": "Some error encoded into JSON"
//                }
//                """.trim();
//
//        HTTP_SERVER.expectResponse(code, body);
//
//        var emails = Stream.generate(this::randomEmail)
//                .limit(5)
//                .toList();
//
//        Assertions.assertThat(HTTP_SERVER.requestsCount()).isZero();
//
//        Assertions.assertThatThrownBy(() -> emailServer.sendBatch(emails))
//                .isInstanceOf(EmailServer.EmailException.class)
//                .hasMessageContaining(body);
//
//        Assertions.assertThat(HTTP_SERVER.requestsCount())
//                .isEqualTo(REQUEST_ATTEMPTS);
//    }

    private SendEmailTestCase sendEmailTestCase() {
        var email = new Email(EmailAddress.ofNameEmail("Some App", "app@app.io"),
                EmailAddress.ofNameEmail("User", "user@user.io"),
                "Some email",
                "some html message",
                "some text message",
                "some tag",
                Map.of());

        var body = JsonMapper.json(new PostmarkEmailServer.PostmarkEmail(
                "Some App <app@app.io>",
                "User <user@user.io>",
                "Some email",
                "some html message",
                "some text message",
                "some tag",
                Map.of(),
                MESSAGE_STREAM
        ));

        var request = new EmailRequestData(Map.of("content-type", "application/json",
                "content-length", contentLength(body),
                "x-postmark-server-token", postmarkToken), body);

        return new SendEmailTestCase(email, request);
    }

    private String contentLength(String body) {
        return String.valueOf(body.getBytes(StandardCharsets.UTF_8).length);
    }

    private SendEmailsTestCase sendEmailsTestCase() {
        var fromEmail = EmailAddress.ofNameEmail("Hairo App", "app@hairo.io");
        var fromEmailString = "Hairo App <app@hairo.io>";

        var email2Tag = TestRandom.string();
        var email2Metadata = Map.of("meta-1", "some secret and useful data");
        var email3Tag = "Some email tag";
        var email3Metadata = Map.of("meta-222", "meta data", "anotherKey", "even more data");

        var emails = List.of(
                new Email(fromEmail,
                        EmailAddress.ofNameEmail("User1", "user1@user.io"),
                        "Some email1",
                        "some html message1",
                        "some text message1",
                        "",
                        Map.of()),
                new Email(fromEmail,
                        EmailAddress.ofNameEmail("User2 < 3", "user2@user.io"),
                        "Some email2",
                        "some html message2",
                        "some text message2",
                        email2Tag,
                        email2Metadata),
                new Email(fromEmail,
                        EmailAddress.ofNameEmail("User3", "user3@user.io"),
                        "Some email3",
                        "some html message3",
                        "some text message3",
                        email3Tag,
                        email3Metadata));

        var firstRequestBody = JsonMapper.json(List.of(
                new PostmarkEmailServer.PostmarkEmail(
                        fromEmailString,
                        "User1 <user1@user.io>",
                        "Some email1",
                        "some html message1",
                        "some text message1",
                        "",
                        Map.of(),
                        MESSAGE_STREAM
                ),
                new PostmarkEmailServer.PostmarkEmail(
                        fromEmailString,
                        "\"User2 < 3\" <user2@user.io>",
                        "Some email2",
                        "some html message2",
                        "some text message2",
                        email2Tag,
                        email2Metadata,
                        MESSAGE_STREAM
                )));

        var firstRequest = new EmailBatchRequestData("/email/batch", "POST",
                emailBatchHeaders(firstRequestBody), firstRequestBody);

        var secondRequestBody = JsonMapper.json(List.of(
                new PostmarkEmailServer.PostmarkEmail(
                        fromEmailString,
                        "User3 <user3@user.io>",
                        "Some email3",
                        "some html message3",
                        "some text message3",
                        email3Tag,
                        email3Metadata,
                        MESSAGE_STREAM
                )));

        var secondRequest = new EmailBatchRequestData("/email/batch", "POST",
                emailBatchHeaders(secondRequestBody), secondRequestBody);

        return new SendEmailsTestCase(emails, List.of(firstRequest, secondRequest));
    }

    private Map<String, String> emailBatchHeaders(String requestBody) {
        return Map.of("content-type", "application/json",
                "content-length", contentLength(requestBody),
                "x-postmark-server-token", postmarkToken);
    }

    private Email randomEmail() {
        return new Email(EmailAddress.ofEmptyName(TestRandom.name()), EmailAddress.ofEmptyName(TestRandom.name()),
                TestRandom.string(), TestRandom.string(), TestRandom.string(), TestRandom.string(),
                Map.of(TestRandom.string(), TestRandom.string()));
    }

    private record SendEmailTestCase(Email email, EmailRequestData request) {
    }

    private record SendEmailsTestCase(List<Email> emails, List<EmailBatchRequestData> requests) {
    }

    private record EmailRequestData(Map<String, String> headers,
                                    String body) {
    }


    private record EmailBatchRequestData(String url,
                                         String method,
                                         Map<String, String> headers,
                                         String body) {
    }
}
