package hu.therealuhlarzoltan.expensables.api.microservices.events;

public class CrudEvent<K, T> extends  Event<K, T> {

    public enum Type {
        CREATE,
        UPDATE,
        DELETE,
    }

    private final Type eventType;

    public CrudEvent(Type eventType, K key, T data) {
        super(key, data);
        this.eventType = eventType;
    }

    @Override
    public Type getEventType() {
        return eventType;
    }



}