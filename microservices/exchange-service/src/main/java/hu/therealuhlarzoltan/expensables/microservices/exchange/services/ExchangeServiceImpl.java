package hu.therealuhlarzoltan.expensables.microservices.exchange.services;

import hu.therealuhlarzoltan.expensables.api.microservices.core.exchange.ExchangeRequest;
import hu.therealuhlarzoltan.expensables.api.microservices.core.exchange.ExchangeResponse;
import hu.therealuhlarzoltan.expensables.microservices.exchange.components.ExchangeGateway;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ExchangeServiceImpl implements ExchangeService {
    private final ExchangeGateway exchangeGateway;

    @Override
    public Mono<ExchangeResponse> exchangeCurrency(ExchangeRequest exchangeRequest) {
        return exchangeGateway.exchangeCurrency(exchangeRequest.getFromCurrency(), exchangeRequest.getToCurrency(), exchangeRequest.getAmount(), exchangeRequest.getExchangeDate());
    }
}
