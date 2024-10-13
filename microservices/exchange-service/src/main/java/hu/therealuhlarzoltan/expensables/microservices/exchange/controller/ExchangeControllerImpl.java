package hu.therealuhlarzoltan.expensables.microservices.exchange.controller;

import hu.therealuhlarzoltan.expensables.api.microservices.core.exchange.ExchangeController;
import hu.therealuhlarzoltan.expensables.api.microservices.core.exchange.ExchangeRequest;
import hu.therealuhlarzoltan.expensables.api.microservices.core.exchange.ExchangeResponse;
import hu.therealuhlarzoltan.expensables.microservices.exchange.services.ExchangeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class ExchangeControllerImpl implements ExchangeController {
    private final ExchangeService exchangeService;

    @Override
    public Mono<ExchangeResponse> exchangeCurrency(ExchangeRequest exchangeRequest) {
        return exchangeService.exchangeCurrency(exchangeRequest);
    }
}
