package com.binaryigor.main._common.app;

import com.binaryigor.email.factory.EmailFactory;
import com.binaryigor.email.factory.EmailTemplatesFactory;
import com.binaryigor.email.factory.TemplatesEmailFactory;
import com.binaryigor.email.model.EmailTemplates;
import com.binaryigor.main._common.core.AppLanguage;
import com.binaryigor.main._common.core.Emails;
import com.binaryigor.tools.FilePathFinder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class EmailModuleProvider {

    public static EmailFactory factory() {
        return factory(null);
    }

    public static EmailFactory factory(String templatesDirectory) {
        if (templatesDirectory == null || !Files.exists(Path.of(templatesDirectory))) {
            templatesDirectory = FilePathFinder.emailTemplatesUpFromCurrentPath();
        }
        return new TemplatesEmailFactory(templates(templatesDirectory));
    }

    public static EmailTemplates templates(String templatesDirectory) {
        return EmailTemplatesFactory.validatedTemplates(templatesDirectory, Emails.TYPES_VARIABLES,
                Arrays.stream(AppLanguage.values()).map(e -> e.name().toLowerCase()).toList());
    }
}
