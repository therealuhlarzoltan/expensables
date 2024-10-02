package hu.therealuhlarzoltan.expensables.microservices.transactionclient.components.gateways;

import hu.therealuhlarzoltan.expensables.api.microservices.core.exchange.ExchangeResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.net.URI;
import java.time.ZonedDateTime;

public interface ExchangeGateway {
    Mono<ExchangeResponse> makeExchange(String fromCurrency, String toCurrency, BigDecimal amount);
    Mono<ExchangeResponse> makeExchange(String fromCurrency, String toCurrency, BigDecimal amount, ZonedDateTime exchangeDate);
}
