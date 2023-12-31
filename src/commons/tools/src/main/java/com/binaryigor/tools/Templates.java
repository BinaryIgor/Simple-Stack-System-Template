package com.binaryigor.tools;

import java.util.*;
import java.util.regex.Pattern;

public class Templates {

    private static final Pattern VARIABLES_REGEX = Pattern.compile("\\$\\{([^}]+)}");
    private static final Pattern COMPONENTS_REGEX = Pattern.compile("#\\{([^}]+)}");

    public static String rendered(String template, Map<String, String> variables) {
        return rendered(template, variables, Map.of());
    }

    public static String rendered(String template, Map<String, String> variables, Map<String, String> components) {
        String fullTemplate;
        if (components.isEmpty()) {
            fullTemplate = template;
        } else {
            fullTemplate = merged(template, components);
        }

        return replacedVariablesTemplate(fullTemplate, variables);
    }

    public static String merged(String template, Map<String, String> components) {
        var replacedTemplate = template;

        var matcher = COMPONENTS_REGEX.matcher(template);
        while (matcher.find()) {
            var toReplace = matcher.group(0);
            var componentName = matcher.group(1);

            var component = Optional.ofNullable(components.get(componentName))
                    .orElseThrow(() -> new RuntimeException("No component of %s name".formatted(componentName)));

            replacedTemplate = replacedTemplate.replace(toReplace, component);
        }

        return replacedTemplate;
    }


    private static String replacedVariablesTemplate(String template, Map<String, String> variables) {
        if (variables.isEmpty()) {
            return template;
        }

        return VARIABLES_REGEX.matcher(template).replaceAll(m -> {
            var varName = m.group(1);
            return Optional.ofNullable(variables.get(varName))
                    .orElseThrow(() -> new RuntimeException("No variable with %s key".formatted(varName)));
        });
    }

    public static Set<String> variables(String template) {
        return variables(template, VARIABLES_REGEX);
    }

    private static Set<String> variables(String template, Pattern pattern) {
        var variables = new HashSet<String>();

        var matcher = pattern.matcher(template);
        while (matcher.find()) {
            variables.add(matcher.group(1));
        }

        return variables;
    }

    public static Set<String> variables(List<String> templates) {
        var allVariables = new HashSet<String>();
        templates.forEach(t -> allVariables.addAll(variables(t)));
        return allVariables;
    }

    public static Set<String> components(String template) {
        return variables(template, COMPONENTS_REGEX);
    }
}
