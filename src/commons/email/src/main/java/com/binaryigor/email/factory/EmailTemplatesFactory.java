package com.binaryigor.email.factory;

import com.binaryigor.email.EmailTemplatesSource;
import com.binaryigor.email.EmailTemplatesValidator;
import com.binaryigor.email.model.EmailTemplates;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class EmailTemplatesFactory {

    public static EmailTemplates validatedTemplates(String templatesDir,
                                                    Map<String, List<String>> typesVariables,
                                                    Collection<String> requiredLanguages) {
        var templates = EmailTemplatesSource.fromFiles(new File(templatesDir), requiredLanguages);
        EmailTemplatesValidator.validate(templates, typesVariables, requiredLanguages);
        return templates;
    }
}
