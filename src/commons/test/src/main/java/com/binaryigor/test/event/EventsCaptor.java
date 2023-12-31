package com.binaryigor.test.event;

import java.util.List;

public record EventsCaptor<T>(List<T> all) {

    public T last() {
        if (all.isEmpty()) {
            return null;
        }
        return all.get(all.size() - 1);
    }

    public T first() {
        if (all.isEmpty()) {
            return null;
        }
        return all.get(0);
    }

    public void clear() {
        all.clear();
    }
}
