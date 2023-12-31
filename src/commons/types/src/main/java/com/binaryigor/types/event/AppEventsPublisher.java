package com.binaryigor.types.event;

//Synchronous, local(in memory) publisher to simplify certain code flows.
public interface AppEventsPublisher {

    <T> void publish(T event);
}
