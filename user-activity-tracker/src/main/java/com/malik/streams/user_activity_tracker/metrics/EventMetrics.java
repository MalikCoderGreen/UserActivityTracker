package com.malik.streams.user_activity_tracker.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class EventMetrics {

    private final Counter eventsConsumed;
    private final Counter eventsIndexed;
    private final Counter eventsFailed;
    private final Counter eventsDlq;

    public EventMetrics(MeterRegistry registry) {
        this.eventsConsumed = registry.counter("events.consumed");
        this.eventsIndexed  = registry.counter("events.indexed");
        this.eventsFailed   = registry.counter("events.failed");
        this.eventsDlq      = registry.counter("events.dlq");
    }

    public void incrementConsumed() { eventsConsumed.increment(); }
    public void incrementIndexed()  { eventsIndexed.increment(); }
    public void incrementFailed()   { eventsFailed.increment(); }
    public void incrementDlq()      { eventsDlq.increment(); }
}