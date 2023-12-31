package com.binaryigor.tools;


import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FilePathFinder {

    public static String templatesUpFromCurrentPath() {
        return upFromCurrentPath("templates").toString();
    }

    public static String repoRootUpFromCurrentPath() {
        return upFromCurrentPath("src").getParent().toString();
    }

    public static Path upFromCurrentPath(String fileOrDirectory) {
        var current = Paths.get("").toAbsolutePath();
        var start = current;

        try {
            while (current != null) {
                if (dirContains(current, fileOrDirectory)) {
                    return current.resolve(fileOrDirectory);
                }

                current = current.getParent();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        throw new RuntimeException("Can't find %s file or directory, starting from %s"
                .formatted(fileOrDirectory, start));
    }

    private static boolean dirContains(Path dir, String fileOrDirectory) throws Exception {
        return Files.list(dir).anyMatch(p -> p.endsWith(fileOrDirectory));
    }
}
