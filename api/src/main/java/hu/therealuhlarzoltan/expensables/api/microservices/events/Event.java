package hu.therealuhlarzoltan.expensables.api.microservices.events;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.ZonedDateTimeSerializer;

import java.time.ZonedDateTime;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = CrudEvent.class, name = "CrudEvent"),
        @JsonSubTypes.Type(value = AccountEvent.class, name = "AccountEvent")
})
public abstract class Event<K, T> {
    protected final K key;
    protected final T data;
    private final ZonedDateTime eventCreatedAt;

    public Event() {
        this.key = null;
        this.data = null;
        this.eventCreatedAt = ZonedDateTime.now();
    }

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
