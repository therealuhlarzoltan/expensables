package hu.therealuhlarzoltan.expensables.api.microservices.events;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.ZonedDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

import static java.time.ZonedDateTime.now;

public class AccountEvent<K, T> extends Event<K, T> {

    public enum Type {
        DEPOSIT,
        WITHDRAW
    }

    private final Type eventType;

    public AccountEvent() {
        super();
        this.eventType = null;
    }

    public AccountEvent(Type eventType, K key, T data) {
        super(key, data);
        this.eventType = eventType;
    }

    @Override
    public Type getEventType() {
        return eventType;
    }
}

