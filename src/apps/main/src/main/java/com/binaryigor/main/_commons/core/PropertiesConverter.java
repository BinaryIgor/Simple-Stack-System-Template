package com.binaryigor.main._commons.core;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

public class PropertiesConverter {

    private static final String FILE_PREFIX = "file:";

    public static byte[] bytesFromString(String string) {
        if (string == null) {
            return null;
        }
        return Base64.getDecoder().decode(string);
    }

    public static String valueOrFromFile(String property) {
        try {
            if (property.startsWith(FILE_PREFIX)) {
                return Files.readString(Path.of(property.replaceFirst(FILE_PREFIX, ""))).strip();
            }
            return property;
        } catch (Exception e) {
            throw new RuntimeException("Problem while reading %s property...".formatted(property), e);
        }
    }
}