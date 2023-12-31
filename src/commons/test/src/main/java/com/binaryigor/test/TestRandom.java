package com.binaryigor.test;

import com.binaryigor.types.Pair;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestRandom {

    public static final Random RANDOM = new SecureRandom();
    private static final String ALPHABET_STRING = "aAbBcCdDeEfFgGhHiIjJkKlLmMnNoOpPqQrRsStTuUvVwWxXyYzZ";
    private static final char[] ALPHANUMERIC_ALPHABET = (ALPHABET_STRING + "0123456789").toCharArray();
    private static final long DATES_RANGE = TimeUnit.DAYS.toSeconds(365);

    public static <T> List<T> fragmentOrEmpty(T[] items, boolean allowEmpty) {
        return fragmentOrEmpty(Arrays.asList(items), allowEmpty);
    }

    public static <T> List<T> fragmentOrEmpty(List<T> items, boolean allowEmpty) {
        if (items.isEmpty() && !allowEmpty) {
            throw new RuntimeException("Can't return random fragment from empty list");
        }

        if (items.size() <= 1) {
            return items;
        }

        var size = (allowEmpty ? 0 : 1) + RANDOM.nextInt(items.size());

        int start, end;
        if (size == items.size()) {
            if (isTrue()) {
                start = 1;
                end = size;
            } else {
                start = 0;
                end = size - 1;
            }
        } else {
            start = 0;
            end = size;
        }

        return items.subList(start, end);
    }

    public static <T> List<T> fragmentOrEmpty(T[] items) {
        return fragmentOrEmpty(items, true);
    }

    public static <T> List<T> fragmentOrEmpty(List<T> items) {
        return fragmentOrEmpty(items, true);
    }

    public static <T> List<T> fragmentOrEmpty(Collection<T> items, boolean allowEmpty) {
        return fragmentOrEmpty(new ArrayList<>(items), allowEmpty);
    }

    public static <T> List<T> fragment(Collection<T> items) {
        return fragmentOrEmpty(items, false);
    }

    public static <T> List<T> fragment(T[] items) {
        return fragmentOrEmpty(items, false);
    }

    public static <T> List<T> fragment(List<T> items) {
        return fragmentOrEmpty(items, false);
    }

    public static <K, V> Map<K, V> fragment(Map<K, V> items, int size) {
        return fragment(items.entrySet(), size).stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static <T> List<T> fragment(Collection<T> items, int size) {
        if (size > items.size()) {
            throw new RuntimeException("Can't return fragment greater than given items");
        }

        if (size == 0) {
            return List.of();
        }

        var fragment = new LinkedList<T>();

        for (int i = 0; i < size; i++) {
            var item = oneOf(items);
            while (fragment.contains(item)) {
                item = oneOf(items);
            }

            fragment.add(item);
        }

        return fragment;
    }

    public static <T> T oneOfExcluding(Collection<T> items, Collection<T> toExclude) {
        var item = oneOf(items);
        while (toExclude.contains(item) && items.size() > 1) {
            item = oneOf(items);
        }
        return item;
    }

    public static <T> T oneOfExcluding(Collection<T> items, T toExclude) {
        return oneOfExcluding(items, List.of(toExclude));
    }

    public static <T> T oneOfExcluding(T[] items, Collection<T> toExclude) {
        return oneOfExcluding((Collection<T>) List.of(items), toExclude);
    }

    public static <T> T oneOfExcluding(T[] items, T toExclude) {
        return oneOfExcluding(items, List.of(toExclude));
    }

    public static <T> List<T> withoutOneOf(Collection<T> items) {
        var toRemove = oneOf(new ArrayList<>(items));
        return items.stream()
                .filter(i -> !i.equals(toRemove))
                .collect(Collectors.toList());
    }

    public static <T> Pair<T, List<T>> withoutOneOfReturningResult(Collection<T> items) {
        var toRemove = oneOf(new ArrayList<>(items));
        return new Pair<>(toRemove, items.stream()
                .filter(i -> !i.equals(toRemove))
                .collect(Collectors.toList()));
    }

    public static long longValue() {
        return inRange(1L, Integer.MAX_VALUE);
    }

    public static double doubleValue() {
        return inRange(0, Double.MAX_VALUE);
    }

    public static Instant futureInstant(Instant now) {
        return now.plusSeconds(inRange(1, DATES_RANGE)).truncatedTo(ChronoUnit.MILLIS);
    }

    public static Instant futureInstant() {
        return futureInstant(Instant.now());
    }

    public static Instant instant() {
        return instant(Instant.now());
    }

    public static Instant instant(Instant now) {
        if (isTrue()) {
            return futureInstant(now);
        }
        return pastInstant(now);
    }

    public static Instant pastInstant(Instant now) {
        return now.minusSeconds(inRange(1, DATES_RANGE)).truncatedTo(ChronoUnit.MILLIS);
    }

    public static int inRange(int min, int max) {
        return min + RANDOM.nextInt(max - min + 1);
    }

    public static long inRange(long min, long max) {
        long bound = (long) (RANDOM.nextDouble() * (max - min));
        return min + bound;
    }

    public static double inRange(double min, double max) {
        double bound = (RANDOM.nextDouble() * (max - min));
        return min + bound;
    }

    public static String string(int minSize, int maxSize) {
        int size;
        if (minSize == maxSize) {
            size = maxSize;
        } else {
            size = minSize + RANDOM.nextInt(maxSize - minSize);
        }

        var builder = new StringBuilder();

        for (int i = 0; i < size; i++) {
            var idx = RANDOM.nextInt(ALPHANUMERIC_ALPHABET.length);
            builder.append(ALPHANUMERIC_ALPHABET[idx]);
        }

        return builder.toString();
    }

    public static String string() {
        return string(20, 50);
    }

    public static String name() {
        var firstLetter = ALPHABET_STRING.charAt(RANDOM.nextInt(ALPHABET_STRING.length()));
        return firstLetter + string(1, 25);
    }

    public static String string(int size) {
        return string(size, size);
    }

    public static String id() {
        return string(12);
    }

    public static List<String> ids(int size) {
        return Stream.generate(TestRandom::id)
                .limit(size)
                .toList();
    }

    public static List<String> ids() {
        return ids(TestRandom.inRange(5, 20));
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

    public static <T> T oneOf(Collection<T> items) {
        return oneOf(new ArrayList<>(items));
    }

    public static <T> T oneOf(T[] items) {
        return oneOf(List.of(items));
    }

    public static <T extends Enum<?>> T oneOf(Class<T> enumType) {
        return oneOf(enumType.getEnumConstants());
    }

    public static byte[] bytes(int size) {
        var bytes = new byte[size];
        RANDOM.nextBytes(bytes);
        return bytes;
    }

    public static byte[] bytes() {
        return bytes(inRange(10, 50));
    }
}
