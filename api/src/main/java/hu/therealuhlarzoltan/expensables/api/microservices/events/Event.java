package hu.therealuhlarzoltan.expensables.api.microservices.events;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.ZonedDateTimeSerializer;

import java.time.ZonedDateTime;

public abstract class Event<K, T> {
    protected final K key;
    protected final T data;
    private final ZonedDateTime eventCreatedAt;

    public Event(K key, T data) {
        this.key = key;
        this.data = data;
        this.eventCreatedAt = ZonedDateTime.now();
    }

    public K getKey() {
        return key;
    }

    public T getData() {
        return data;
    }

    @JsonSerialize(using = ZonedDateTimeSerializer.class)
    public ZonedDateTime getEventCreatedAt() {
        return eventCreatedAt;
    }

    public abstract Enum<?> getEventType();
}
