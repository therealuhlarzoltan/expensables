package hu.therealuhlarzoltan.expensables.microservices.cashflow.services;

import hu.therealuhlarzoltan.expensables.api.microservices.events.HttpResponseEvent;
import reactor.core.publisher.Mono;

import java.time.Duration;

public interface ResponseListenerService {
    Mono<HttpResponseEvent> waitForResponse(String correlationId, Duration timeout);
}
