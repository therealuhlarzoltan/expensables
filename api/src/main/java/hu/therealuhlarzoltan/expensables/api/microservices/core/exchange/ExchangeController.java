package hu.therealuhlarzoltan.expensables.api.microservices.core.exchange;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import reactor.core.publisher.Mono;

public interface ExchangeController {
    @PostMapping("/api/exchange")
    Mono<ExchangeResponse> exchangeCurrency(@Valid @RequestBody ExchangeRequest exchangeRequest);
}
