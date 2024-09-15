package hu.therealuhlarzoltan.expensables.microservices.transactionclient.components.gateways;

import hu.therealuhlarzoltan.expensables.api.microservices.core.exchange.ExchangeResponse;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.net.URI;

public interface ExchangeGateway {
    Mono<ExchangeResponse> makeExchange(String fromCurrency, String toCurrency, BigDecimal amount);
}
