package com.binaryigor.email;

import com.binaryigor.email.model.EmailTemplate;
import com.binaryigor.email.model.EmailTemplates;
import com.binaryigor.email.model.EmailTranslation;
import com.binaryigor.json.JsonMapper;
import com.binaryigor.tools.CustomFileInterpreter;
import com.binaryigor.tools.Templates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class EmailTemplatesSource {

    public static final String FILE_EXTENSION = ".template";
    public static final String GLOBAL_VARIABLES = "_global_variables.json";
    public static final String GLOBAL_TRANSLATIONS = "_global_translations.json";
    public static final String COMPONENT = "_component";
    private static final Logger log = LoggerFactory.getLogger(EmailTemplatesSource.class);
    private static final String SUBJECT = "subject";
    private static final String CODE_VARIABLES_SECTION = "codeVariables";
    private static final String TRANSLATIONS_SECTION = "translations";
    private static final String TEMPLATE_VARIABLES_SECTION = "templateVariables";
    private static final String HTML_SECTION = "html";
    private static final String TEXT_SECTION = "text";
    private static final List<String> REQUIRED_SECTIONS = List.of(CODE_VARIABLES_SECTION, TRANSLATIONS_SECTION,
            HTML_SECTION, TEXT_SECTION);

    public static EmailTemplates fromFiles(File rootDir, Collection<String> requiredLanguages) {
        var files = rootDir.listFiles();
        if (files == null || files.length == 0) {
            throw new RuntimeException("There are no templates in %s dir".formatted(rootDir));
        }

        var names = Arrays.stream(files)
                .filter(f -> !f.isDirectory() && f.getName().endsWith(FILE_EXTENSION))
                .map(f -> f.getName().replace(FILE_EXTENSION, ""))
                .toList();

        if (names.isEmpty()) {
            throw new RuntimeException("There are no templates in %s dir. They need to end with %s extension."
                    .formatted(rootDir, FILE_EXTENSION));
        }

        return fromFiles(rootDir, names, requiredLanguages);
    }

    public static EmailTemplates fromFiles(File rootDir,
                                           Collection<String> names,
                                           Collection<String> requiredLanguages) {
        log.info("Searching for {} templates in {} dir...", names, rootDir);

        var templates = templates(rootDir, names, requiredLanguages);

        var globalVariables = globalVariables(new File(rootDir, GLOBAL_VARIABLES).toPath());
        var globalTranslations = globalTranslations(new File(rootDir, GLOBAL_TRANSLATIONS).toPath());

        return new EmailTemplates(templates, globalVariables, globalTranslations);
    }

    private static Map<String, EmailTemplate> templates(File rootDir, Collection<String> names,
                                                        Collection<String> requiredLanguages) {
        return names.stream()
                .collect(Collectors.toMap(Function.identity(),
                        t -> template(rootDir, t, requiredLanguages)));
    }

    private static EmailTemplate template(File rootDir, String templateName,
                                          Collection<String> requiredLanguages) {
        var rawTemplate = rawTemplate(rootDir, templateName);

        try {
            var components = new HashMap<String, String>();

            var template = parsedTemplate(templateName, rawTemplate, requiredLanguages);

            loadComponents(rootDir, components,
                    Templates.components(template.html()));

            var fullHtmlTemplate = Templates.merged(template.html(), components);

            loadComponents(rootDir, components,
                    Templates.components(template.text()));

            var fullTextTemplate = Templates.merged(template.text(), components);

            return template.withHtmlAndText(fullHtmlTemplate, fullTextTemplate);
        } catch (Exception e) {
            throw new RuntimeException("Can't parse %s template of content: %s"
                    .formatted(templateName, rawTemplate), e);
        }
    }

    private static String rawTemplate(File rootDir, String templateName) {
        try {
            var path = Path.of(rootDir.getAbsolutePath(), templateName + FILE_EXTENSION);
            return Files.readString(path);
        } catch (Exception e) {
            throw new RuntimeException("Can't load %s template".formatted(templateName), e);
        }
    }

    private static EmailTemplate parsedTemplate(String type, String rawTemplate,
                                                Collection<String> requiredLanguages) {
        var sections = CustomFileInterpreter.sections(rawTemplate);

        if (!sections.keySet().containsAll(REQUIRED_SECTIONS)) {
            throw new RuntimeException("%s sections found: %s, but required are: %s"
                    .formatted(type, sections, REQUIRED_SECTIONS));
        }

        var codeVariables = CustomFileInterpreter.listSection(sections.get(CODE_VARIABLES_SECTION));

        var languagesTranslations = emailTranslations(type, sections.get(TRANSLATIONS_SECTION), requiredLanguages);

        var templateVariables = CustomFileInterpreter.variablesSection(
                sections.getOrDefault(TEMPLATE_VARIABLES_SECTION, ""));

        return new EmailTemplate(codeVariables, languagesTranslations,
                templateVariables, sections.get(HTML_SECTION), sections.get(TEXT_SECTION));
    }

    private static Map<String, EmailTranslation> emailTranslations(String type,
                                                                   String translationsSection,
                                                                   Collection<String> requiredLanguages) {
        var languagesTranslationsSections = CustomFileInterpreter.sections(translationsSection);

        var languagesTranslations = new HashMap<String, EmailTranslation>();

        for (var l : requiredLanguages) {
            var lowered = l.toLowerCase();
            var lTranslations = languagesTranslationsSections.get(lowered);

            if (lTranslations == null) {
                throw new RuntimeException("%s template doesn't have translations for %s language"
                        .formatted(type, lowered));
            }

            var lMessages = CustomFileInterpreter.variablesSection(lTranslations);

            var subject = lMessages.get(SUBJECT);

            if (subject == null) {
                throw new RuntimeException("%s doesn't have %s required variable for %s language"
                        .formatted(type, SUBJECT, lowered));
            }

            languagesTranslations.put(l, new EmailTranslation(subject, lMessages));
        }

        return languagesTranslations;
    }

    private static void loadComponents(File rootDir,
                                       Map<String, String> components,
                                       Set<String> neededComponents) {
        for (var name : neededComponents) {
            if (components.containsKey(name)) {
                continue;
            }
            try {
                var path = Path.of(rootDir.getAbsolutePath(), COMPONENT, name);
                var component = Files.readString(path);
                components.put(name, component);
            } catch (Exception e) {
                throw new RuntimeException("Can't load %s component".formatted(name));
            }
        }
    }

    private static Map<String, String> globalVariables(Path path) {
        try {
            var json = Files.readString(path);
            return JsonMapper.map(json, String.class);
        } catch (Exception e) {
            return Map.of();
        }
    }

    private static Map<String, Map<String, String>> globalTranslations(Path path) {
        try {
            var json = Files.readString(path);
            return JsonMapper.mapMap(json, String.class, String.class, String.class);
        } catch (Exception e) {
            return Map.of();
        }
    }

}
