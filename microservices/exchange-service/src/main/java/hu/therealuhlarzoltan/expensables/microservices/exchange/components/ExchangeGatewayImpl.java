package hu.therealuhlarzoltan.expensables.microservices.exchange.components;

import hu.therealuhlarzoltan.expensables.api.microservices.core.account.Account;
import hu.therealuhlarzoltan.expensables.api.microservices.core.exchange.ExchangeRequest;
import hu.therealuhlarzoltan.expensables.api.microservices.core.exchange.ExchangeResponse;
import hu.therealuhlarzoltan.expensables.api.microservices.exceptions.ServiceResponseException;
import io.github.cdimascio.dotenv.Dotenv;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.net.URI;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeoutException;

import static hu.therealuhlarzoltan.expensables.microservices.exchange.components.WebClientRequests.*;

@Component
@RequiredArgsConstructor
public class ExchangeGatewayImpl implements ExchangeGateway {
    private final WebClient webClient;

    @CircuitBreaker(name = "exchangeApi", fallbackMethod = "handleFallback")
    @Retry(name = "exchangeApi")
    @TimeLimiter(name = "exchangeApi", fallbackMethod = "handleTimeoutFallback")
    @Override
    public Mono<ExchangeResponse> exchangeCurrency(String fromCurrency, String toCurrency, BigDecimal amount, ZonedDateTime date) {
        String dateString = date.toLocalDate().toString();
        String relativeUri = UriComponentsBuilder.fromUriString("/convert")
                .queryParam("from", fromCurrency)
                .queryParam("to", toCurrency)
                .queryParam("amount", amount)
                .queryParam("date", dateString)
                .build().toUriString();
        return getForConversionReactive(relativeUri)
                .map(response -> {
                    response.setToCurrency(toCurrency);
                    response.setFromCurrency(fromCurrency);
                    response.setAmount(amount);
                    response.setExchangeDate(date);
                    return response;
                });
    }

    // Handling timeouts
    public Mono<ExchangeResponse> handleTimeoutFallback(String fromCurrency, String toCurrency, BigDecimal amount, ZonedDateTime date, TimeoutException ex) {
        return Mono.error(new ServiceResponseException("Dependent service call failed", HttpStatus.FAILED_DEPENDENCY));
    }


    // Handling exceptions
    public Mono<Account> handleFallback(String fromCurrency, String toCurrency, BigDecimal amount, ZonedDateTime date, Throwable ex) {
        // Only handling 5xx server errors here
        if (ex instanceof WebClientResponseException && ((WebClientResponseException) ex).getStatusCode().is5xxServerError()) {
            return Mono.error(new ServiceResponseException("Dependent service call failed", HttpStatus.FAILED_DEPENDENCY));
        } else if (ex instanceof CallNotPermittedException) {
            return Mono.error(new ServiceResponseException("Service unavailable", HttpStatus.SERVICE_UNAVAILABLE));
        }
        // "Re-throwing" the exception if it's not a 5xx error
        return Mono.error(ex);
    }
}
