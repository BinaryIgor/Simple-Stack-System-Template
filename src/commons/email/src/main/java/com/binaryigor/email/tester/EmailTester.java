package com.binaryigor.email.tester;

import com.binaryigor.email.EmailTemplatesSource;
import com.binaryigor.email.factory.TemplatesEmailFactory;
import com.binaryigor.email.model.*;
import com.binaryigor.email.server.PostmarkEmailServer;
import com.binaryigor.tools.FilePathFinder;
import com.binaryigor.tools.Randoms;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;

public class EmailTester {

    private static final Logger log = LoggerFactory.getLogger(EmailTester.class);
    private static final List<String> REQUIRED_LANGUAGES = List.of("PL", "EN");
    private static final EmailAddress FROM_EMAIL_ADDRESS = new EmailAddress("App", "app@binaryigor.com");
    private static final String EMAIL_TAG = "test";
    private static final String SEPARATOR = "-".repeat(80);

    public static void main(String[] args) {
        log.info("Searching for templates dir...");
        var rootDir = FilePathFinder.emailTemplatesUpFromCurrentPath();

        var codeVariables = codeVariables();
        var renderingResultDir = System.getenv("RENDERING_RESULT_DIR");

        var sendTo = System.getenv("SEND_TO");
        EmailAddress to;
        Consumer<Collection<Email>> emailsSender;

        if (sendTo == null) {
            log.info("SEND_TO not set, emails will be only rendered, not send");
            to = EmailAddress.ofEmptyName("user@gmail.com");
            emailsSender = null;
        } else {
            log.info("All rendered emails will be sent to {}", sendTo);
            to = EmailAddress.ofEmptyName(sendTo);
            emailsSender = emailsSender();
        }

        var templates = templates(new File(rootDir, "email"));

        var factory = new TemplatesEmailFactory(templates);

        var consoleOutput = renderingResultDir == null;

        var emails = new ArrayList<Email>();

        System.out.printf("Rendering emails to %s...%n", consoleOutput ? "console" : renderingResultDir);
        templates.templates().forEach((n, t) -> {
            try {
                System.out.printf("Rendering %s with codeVariables: %s%n", n, codeVariables);
                System.out.println();

                for (var l : REQUIRED_LANGUAGES) {
                    System.out.printf("Language: %s%n", l);
                    System.out.println();

                    fillLackingVariables(codeVariables, t);

                    var email = factory.newEmail(new NewEmailTemplate(FROM_EMAIL_ADDRESS, to, l, n, codeVariables,
                            EMAIL_TAG, Map.of()));

                    System.out.println();
                    System.out.printf("Subject: %s%n", email.subject());
                    System.out.println(SEPARATOR);

                    if (consoleOutput) {
                        System.out.println("Html:");
                        System.out.println(email.htmlMessage());
                        System.out.println(SEPARATOR);
                    } else {
                        saveEmailToFile(renderingResultDir, n, l, email.htmlMessage(), true);
                    }

                    if (consoleOutput) {
                        System.out.println("Text:");
                        System.out.println(email.textMessage());
                        System.out.println(SEPARATOR);
                    } else {
                        saveEmailToFile(renderingResultDir, n, l, email.textMessage(), false);
                    }

                    System.out.println();

                    emails.add(email);
                }
            } catch (Exception e) {
                System.out.printf("Problem while rendering: %s...%n", n);
                e.printStackTrace(System.out);
                System.out.close();
                throw new RuntimeException("Problem while rendering %s email".formatted(n), e);
            }

        });

        Optional.ofNullable(emailsSender)
                .ifPresent(s -> sendEmails(emailsSender, emails));

        System.out.close();
    }

    private static Map<String, String> codeVariables() {
        var codeVariables = System.getenv("CODE_VARIABLES");

        var variables = new HashMap<String, String>();
        if (codeVariables == null) {
            return variables;
        }

        for (var v : codeVariables.split(",")) {
            var kv = v.split("=", 2);
            if (kv.length != 2) {
                throw new RuntimeException("Invalid key=value format for: " + v);
            }

            variables.put(kv[0].strip(), kv[1].strip());
        }

        return variables;
    }

    private static EmailTemplates templates(File templatesDir) {
        var templatesNames = System.getenv("TEMPLATES_NAMES");

        if (templatesNames == null) {
            System.out.println("No TEMPLATES_NAMES given, rendering all...");
            return EmailTemplatesSource.fromFiles(templatesDir, REQUIRED_LANGUAGES);
        }

        var templates = Arrays.stream(templatesNames.split(","))
                .map(String::strip)
                .toList();

        System.out.printf("Rendering only given template: %s%n", templates);
        System.out.println();

        return EmailTemplatesSource.fromFiles(templatesDir, templates);
    }

    private static Consumer<Collection<Email>> emailsSender() {
        var emailServer = new PostmarkEmailServer(System.getenv("EMAIL_SERVER_TOKEN"));
        return emailServer::sendBatch;
    }

    private static void fillLackingVariables(Map<String, String> codeVariables, EmailTemplate template) {
        template.codeVariables().forEach(v -> {
            if (!codeVariables.containsKey(v)) {
                var value = Randoms.hash(5);
                System.out.printf("Lacking %s variable, filling it with random value of : %s%n", v, value);
                codeVariables.put(v, value);
            }
        });
    }

    private static void sendEmails(Consumer<Collection<Email>> emailsSender, Collection<Email> emails) {
        try {
            System.out.println();
            System.out.println("Sending all emails...");

            emailsSender.accept(emails);

            System.out.println("Emails send.");
            System.out.println();
        } catch (Exception e) {
            System.out.println("Problem while sending...");
            e.printStackTrace(System.out);
        }
    }

    private static void saveEmailToFile(String renderingResultDir,
                                        String templateName,
                                        String language,
                                        String email,
                                        boolean html) throws Exception {
        var path = Paths.get(renderingResultDir, templateName + "_" + language + (html ? ".html" : ".txt"));
        System.out.println();
        System.out.printf("Saving %s email to %s...%n", templateName, path);

        Files.writeString(path, email);

        System.out.println();
    }
}
