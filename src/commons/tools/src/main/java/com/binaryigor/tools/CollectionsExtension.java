package com.binaryigor.tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

public class CollectionsExtension {

    public static <T> List<List<T>> toBuckets(Collection<T> collection, int bucketSize) {
        var lists = new ArrayList<List<T>>();

        var currentList = new ArrayList<T>();

        for (var e : collection) {
            currentList.add(e);
            if (currentList.size() == bucketSize) {
                lists.add(currentList);
                currentList = new ArrayList<>();
            }
        }

        if (!currentList.isEmpty()) {
            lists.add(currentList);
        }

        return lists;
    }

    @SafeVarargs
    public static <T> List<T> mergedCollections(Collection<T>... collections) {
        var merged = new ArrayList<T>();
        for (var c : collections) {
            merged.addAll(c);
        }
        return merged;
    }

    @SafeVarargs
    public static <T> List<T> mergedStreams(Stream<T>... streams) {
        var merged = new ArrayList<T>();
        for (var s : streams) {
            s.forEach(merged::add);
        }
        return merged;
    }

    public static <P, T> List<T> mapToList(Collection<P> collection,
                                           Function<P, T> mapper) {
        return collection.stream().map(mapper).toList();
    }

    public static <P, T> List<T> mapNonNullToList(Collection<P> collection,
                                                  Function<P, T> mapper) {
        return collection.stream().map(mapper).filter(Objects::nonNull).toList();
    }
}
