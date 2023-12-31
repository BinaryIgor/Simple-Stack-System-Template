package com.binaryigor.test.event;

import com.binaryigor.types.event.AppEvents;

import java.util.ArrayList;

public class TestAppEvents {

    public static <T> EventsCaptor<T> subscribe(AppEvents events, Class<T> event) {
        var captured = new ArrayList<T>();
        events.subscribe(event, captured::add);
        return new EventsCaptor<>(captured);
    }

    public static <T> EventsCaptor<T> subscribeThrowing(AppEvents events,
                                                        Class<T> event,
                                                        RuntimeException exception) {
        var captured = new ArrayList<T>();
        events.subscribe(event, e -> {
            captured.add(e);
            throw exception;
        });
        return new EventsCaptor<>(captured);
    }
}
