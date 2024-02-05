package com.binaryigor.main._common.infra;

import java.util.Collection;
import java.util.function.Function;

public class JooqTools {

    public static <T> String[] collectionToStringArray(Collection<T> collection,
                                                       Function<T, String> mapper) {
        var array = new String[collection.size()];
        return collection.stream()
                .map(mapper)
                .toList()
                .toArray(array);
    }
}
