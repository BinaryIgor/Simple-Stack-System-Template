package com.binaryigor.types.event;

public interface AppEvents {

    <T> void subscribe(Class<T> event, Subscriber<T> subscriber);

    AppEventsPublisher publisher();
}
