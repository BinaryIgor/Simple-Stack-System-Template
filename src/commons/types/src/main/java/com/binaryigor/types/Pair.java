package com.binaryigor.types;

public record Pair<F, S>(F first, S second) {

    public boolean contains(Object data) {
        return first.equals(data) || second.equals(data);
    }

    public <T> Pair<T, S> withFirst(T first) {
        return new Pair<>(first, second);
    }

    public <T> Pair<F, T> withSecond(T second) {
        return new Pair<>(first, second);
    }
}
