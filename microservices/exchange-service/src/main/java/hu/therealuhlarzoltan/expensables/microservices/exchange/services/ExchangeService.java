package hu.therealuhlarzoltan.expensables.microservices.exchange.services;

import hu.therealuhlarzoltan.expensables.api.microservices.core.exchange.ExchangeRequest;
import hu.therealuhlarzoltan.expensables.api.microservices.core.exchange.ExchangeResponse;
import reactor.core.publisher.Mono;

public interface ExchangeService {
    Mono<ExchangeResponse> exchangeCurrency(ExchangeRequest exchangeRequest);
}
