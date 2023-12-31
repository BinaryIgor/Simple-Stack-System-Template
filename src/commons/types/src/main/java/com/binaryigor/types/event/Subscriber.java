package com.binaryigor.types.event;

public interface Subscriber<T> {
    void onEvent(T event);
}
