package com.binaryigor.types;

public record Triple<F, S, T>(F first,
                              S second,
                              T third) {

    public <N> Triple<N, S, T> withFirst(N first) {
        return new Triple<>(first, second, third);
    }

    public <N> Triple<F, N, T> withSecond(N second) {
        return new Triple<>(first, second, third);
    }

    public <N> Triple<F, S, N> withThird(N third) {
        return new Triple<>(first, second, third);
    }
}
