package com.binaryigor.test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

public class TestClock extends Clock {

    private final Instant initialTime;
    private Instant instant;

    public TestClock(Instant instant) {
        initialTime = instant;
        this.instant = instant;
    }

    public TestClock() {
        this(Instant.now().truncatedTo(ChronoUnit.MILLIS));
    }

    public void setInitialTime() {
        instant = initialTime;
    }

    @Override
    public ZoneId getZone() {
        return ZoneId.of("UTC");
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return this;
    }

    @Override
    public Instant instant() {
        return instant;
    }

    public void setTime(Instant instant) {
        this.instant = instant;
    }

    public void moveForward(Duration duration) {
        instant = instant.plus(duration);
    }

    public void moveForwardByReasonableAmount() {
        moveForward(Duration.ofMinutes(1));
    }

    public void moveBack(Duration duration) {
        instant = instant.minus(duration);
    }

    public Instant instantPlus(Duration duration) {
        return instant.plus(duration);
    }

    public Instant instantPlusMinutes(int minutes) {
        return instantPlus(Duration.ofMinutes(minutes));
    }

    public Instant instantMinus(Duration duration) {
        return instant.minus(duration);
    }

}
