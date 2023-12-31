package com.binaryigor.tools;

import com.binaryigor.test.TestRandom;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CollectionsExtensionTest {

    @Test
    void shouldReturnMultipleBuckets() {
        var testCase = prepareMultipleBucketsTestCase();

        Assertions.assertThat(CollectionsExtension.toBuckets(testCase.collection, testCase.bucketSize))
                .isEqualTo(testCase.expected);
    }

    @Test
    void shouldReturnSingleBucket() {
        var testCase = prepareSingleSmallerBucketTestCase();

        Assertions.assertThat(CollectionsExtension.toBuckets(testCase.collection, testCase.bucketSize))
                .isEqualTo(testCase.expected);
    }

    private TestCase<Long> prepareMultipleBucketsTestCase() {
        var collection = Stream.generate(TestRandom::longValue)
                .limit(TestRandom.inRange(5, 10))
                .collect(Collectors.toList());

        var bucketSize = collection.size() / 2 + 1;

        var buckets = List.of(collection.subList(0, bucketSize),
                collection.subList(bucketSize, collection.size()));

        return new TestCase<>(collection, bucketSize, buckets);
    }

    private TestCase<String> prepareSingleSmallerBucketTestCase() {
        var collection = List.of("A", "B", "C");
        var bucketSize = collection.size() + 1;

        return new TestCase<>(collection, bucketSize, List.of(collection));
    }

    record TestCase<T>(Collection<T> collection,
                       int bucketSize,
                       List<List<T>> expected) {
    }
}
