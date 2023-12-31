package com.binaryigor.tools;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CustomFileInterpreter {

    private static final String SECTION_START = "{";
    private static final String SECTION_END = "}";

    public static Map<String, String> sections(String fileContent) {
        var sectionsContents = new LinkedHashMap<String, String>();

        String currentSectionName = null;
        var currentSectionStripIdx = -1;
        var currentSection = new StringBuilder();

        var nested = false;

        for (var l : fileContent.strip().split(System.lineSeparator())) {
            if (currentSectionName == null && l.endsWith(SECTION_START)) {
                currentSectionName = l.substring(0, l.lastIndexOf(SECTION_START)).strip();
                currentSection = new StringBuilder();
                continue;
            }

            if (l.endsWith(SECTION_START)) {
                nested = true;
            }

            if (!l.isBlank() && currentSectionName == null) {
                throw new RuntimeException("""
                        Every section need to be opened with %s and closed with %s on the new line,
                        but it wasn't for the next line (nested: %b): %s
                        """.formatted(SECTION_START, SECTION_END, nested, l));
            }

            if (!l.isBlank() && 0 > currentSectionStripIdx) {
                var stripped = l.stripLeading();
                currentSectionStripIdx = l.length() - stripped.length();
            }

            var toAppend = sanitizedLine(l, currentSectionStripIdx) + System.lineSeparator();

            if (l.strip().equals(SECTION_END)) {
                if (nested) {
                    currentSection.append(toAppend);
                    nested = false;
                } else {
                    sectionsContents.put(currentSectionName, currentSection.toString().strip());
                    currentSectionName = null;
                    currentSectionStripIdx = -1;
                }
            } else {
                currentSection.append(toAppend);
            }

        }

        return sectionsContents;
    }

    private static String sanitizedLine(String line, int currentStripIdx) {
        if (line.length() > currentStripIdx && currentStripIdx > 0) {
            return line.substring(currentStripIdx);
        }
        return line;
    }

    public static Map<String, String> variablesSection(String section) {
        var variables = new LinkedHashMap<String, String>();

        for (var l : section.split("\n")) {
            var keyValue = l.split(":", 2);
            if (keyValue.length > 1) {
                variables.put(keyValue[0].strip(), keyValue[1].strip());
            }
        }

        return variables;
    }

    public static List<String> listSection(String section) {
        return Arrays.stream(section.split(","))
                .map(String::strip)
                .toList();
    }
}
