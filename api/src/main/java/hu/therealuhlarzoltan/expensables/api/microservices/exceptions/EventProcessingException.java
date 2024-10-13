package hu.therealuhlarzoltan.expensables.api.microservices.exceptions;

public class EventProcessingException extends RuntimeException {
    public EventProcessingException(String message) {
        super(message);
    }
}
