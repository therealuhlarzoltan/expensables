package hu.therealuhlarzoltan.expensables.microservices.cashflow.components.gateways;

import hu.therealuhlarzoltan.expensables.api.microservices.core.exchange.ExchangeRequest;
import hu.therealuhlarzoltan.expensables.api.microservices.core.exchange.ExchangeResponse;
import hu.therealuhlarzoltan.expensables.api.microservices.exceptions.ServiceResponseException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeoutException;

import static hu.therealuhlarzoltan.expensables.microservices.cashflow.components.gateways.WebClientRequests.*;

@Component
@RequiredArgsConstructor
public class ExchangeGatewayImpl implements ExchangeGateway {
    private final Logger LOG = LoggerFactory.getLogger(ExchangeGatewayImpl.class);
    @Value("${app.exchange-service-url}")
    private String EXCHANGE_SERVICE_URL;

    @Retry(name = "exchangeService")
    @TimeLimiter(name = "exchangeService", fallbackMethod = "handleTimeoutFallback")
    @CircuitBreaker(name = "exchangeService", fallbackMethod = "handleFallback")
    @Override
    public Mono<ExchangeResponse> makeExchange(String fromCurrency, String toCurrency, BigDecimal amount) {
        URI url = UriComponentsBuilder
                .fromUriString(EXCHANGE_SERVICE_URL + "/api/exchange")
                .build().toUri();
        ExchangeRequest requestBody = ExchangeRequest.builder()
                .fromCurrency(fromCurrency)
                .toCurrency(toCurrency)
                .amount(amount)
                .build();
        return postForSingleReactive(url, requestBody, ExchangeResponse.class);
    }

    @Retry(name = "exchangeService")
    @TimeLimiter(name = "exchangeService", fallbackMethod = "handleTimeoutFallback")
    @CircuitBreaker(name = "exchangeService", fallbackMethod = "handleFallback")
    @Override
    public Mono<ExchangeResponse> makeExchange(String fromCurrency, String toCurrency, BigDecimal amount, ZonedDateTime exchangeDate) {
        URI url = UriComponentsBuilder
                .fromUriString(EXCHANGE_SERVICE_URL + "/api/exchange")
                .build().toUri();
        ExchangeRequest requestBody = ExchangeRequest.builder()
                .fromCurrency(fromCurrency)
                .toCurrency(toCurrency)
                .amount(amount)
                .exchangeDate(exchangeDate)
                .build();
        return postForSingleReactive(url, requestBody, ExchangeResponse.class);
    }

    //Handling timeouts
    public Mono<ExchangeResponse> handleTimeoutFallback(String fromCurrency, String toCurrency, BigDecimal amount, TimeoutException ex) {
        return Mono.error(new ServiceResponseException("Dependent service call failed", HttpStatus.FAILED_DEPENDENCY));
    }

    public Mono<ExchangeResponse> handleFallback(String fromCurrency, String toCurrency, BigDecimal amount, Throwable ex) {
        //Only handling 5xx server errors here
        if (ex instanceof WebClientResponseException && ((WebClientResponseException) ex).getStatusCode().is5xxServerError()) {
            return Mono.error(new ServiceResponseException("Dependent service call failed", HttpStatus.FAILED_DEPENDENCY));
        } else if (ex instanceof CallNotPermittedException) {
            return Mono.error(new ServiceResponseException("Service unavailable", HttpStatus.SERVICE_UNAVAILABLE));
        }
        //"Re-throwing" the exception if it's not a 5xx error
        return Mono.error(ex);
    }
}
