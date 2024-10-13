package hu.therealuhlarzoltan.expensables.microservices.cashflow.components.gateways;

import hu.therealuhlarzoltan.expensables.api.microservices.core.exchange.ExchangeResponse;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public interface ExchangeGateway {
    Mono<ExchangeResponse> makeExchange(String fromCurrency, String toCurrency, BigDecimal amount);
    Mono<ExchangeResponse> makeExchange(String fromCurrency, String toCurrency, BigDecimal amount, ZonedDateTime exchangeDate);
}
