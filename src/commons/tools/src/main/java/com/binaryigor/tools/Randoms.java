package com.binaryigor.tools;

import java.security.SecureRandom;
import java.util.List;

public class Randoms {

    public static final SecureRandom RANDOM = new SecureRandom();
    private static final char[] ALPHANUMERIC_ALPHABET = "aAbBcCdDeEfFgGhHiIjJkKlLmMnNoOpPqQrRsStTuUvVwWxXyYzZ0123456789".toCharArray();

    public static String hash(char[] alphabet, int size) {
        var builder = new StringBuilder();

        for (int i = 0; i < size; i++) {
            var idx = RANDOM.nextInt(alphabet.length);
            builder.append(alphabet[idx]);
        }

        return builder.toString();
    }

    public static String hash(int minSize, int maxSize) {
        int size;
        if (minSize == maxSize) {
            size = maxSize;
        } else {
            size = minSize + RANDOM.nextInt(maxSize - minSize);
        }
        return hash(ALPHANUMERIC_ALPHABET, size);
    }

    public static String hash() {
        return hash(10, 30);
    }

    public static String hash(int size) {
        return hash(size, size);
    }

    public static boolean isTrue() {
        return RANDOM.nextBoolean();
    }

    public static <T> T oneOf(List<T> items) {
        if (items.isEmpty()) {
            throw new RuntimeException("Can't return random item from empty list");
        }
        return items.get(RANDOM.nextInt(items.size()));
    }
}
