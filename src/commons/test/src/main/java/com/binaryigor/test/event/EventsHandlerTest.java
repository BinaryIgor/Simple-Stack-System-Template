package com.binaryigor.test.event;

import com.binaryigor.types.event.AppEvents;
import com.binaryigor.types.event.AppEventsPublisher;
import com.binaryigor.types.event.InMemoryEvents;
import org.junit.jupiter.api.BeforeEach;

public abstract class EventsHandlerTest {

    protected final AppEvents appEvents = new InMemoryEvents();
    protected final AppEventsPublisher appEventsPublisher = appEvents.publisher();

    protected abstract void setup(AppEvents events);

    @BeforeEach
    void setup() {
        setup(appEvents);
    }
}
