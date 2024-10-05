package hu.therealuhlarzoltan.expensables.microservices.exchange.components;

import hu.therealuhlarzoltan.expensables.api.microservices.core.exchange.ExchangeRequest;
import hu.therealuhlarzoltan.expensables.api.microservices.core.exchange.ExchangeResponse;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public interface ExchangeGateway {
    Mono<ExchangeResponse> exchangeCurrency(String fromCurrency, String toCurrency, BigDecimal amount, ZonedDateTime date);
}
