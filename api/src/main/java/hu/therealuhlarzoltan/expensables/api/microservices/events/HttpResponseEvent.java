package hu.therealuhlarzoltan.expensables.api.microservices.events;

public class HttpResponseEvent extends Event<String, ResponsePayload> {

    private final Type eventType;

    public enum Type {
        SUCCESS,
        ERROR,
    }

    public HttpResponseEvent() {
        super();
        this.eventType = null;
    }

    public HttpResponseEvent(Type type, String key, ResponsePayload payload) {
        super(key, payload);
        this.eventType = type;
    }

    public Type getEventType() {
        return eventType;
    }

}
