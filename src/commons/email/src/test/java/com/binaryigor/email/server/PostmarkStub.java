package com.binaryigor.email.server;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;

import java.util.Map;

public class PostmarkStub {

    private final WireMockServer server;

    public PostmarkStub(WireMockServer server) {
        this.server = server;
    }

//    public void stubEmailRequest(String body,
//                                 Map<String, String> headers) {
//        var expectations = WireMock.request("POST",
//                WireMock.urlEqualTo("email"));
//
//        for (var e : headers.entrySet()) {
//            System.out.println("Header..." + e);
//            expectations = expectations.withHeader(e.getKey(), WireMock.equalTo(e.getValue() + "__"));
//        }
//
//        expectations.withRequestBody(WireMock.equalTo(body + "___"));
//
//        expectations.willReturn(WireMock.aResponse()
//                .withStatus(500));
//
//        server.stubFor(expectations);
//    }

    public void verifyEmailRequest(String body,
                                   Map<String, String> headers) {
        var requestPattern = WireMock.postRequestedFor(WireMock.urlEqualTo("/email"))
                .withRequestBody(WireMock.equalTo(body));

        headers.forEach((k, v) -> {
            requestPattern.withHeader(k, WireMock.equalTo(v));
        });

        server.verify(1, requestPattern);
    }
}
