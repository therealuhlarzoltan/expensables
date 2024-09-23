package hu.therealuhlarzoltan.expensables.api.microservices.events;

public class CrudEvent<K, T> extends  Event<K, T> {

    public enum Type {
        CREATE,
        UPDATE,
        DELETE,
        DELETE_ALL
    }

    private final Type eventType;

    public CrudEvent() {
        super();
        this.eventType = null;
    }

    public CrudEvent(Type eventType, K key, T data) {
        super(key, data);
        this.eventType = eventType;
    }

    @Override
    public Type getEventType() {
        return eventType;
    }
}