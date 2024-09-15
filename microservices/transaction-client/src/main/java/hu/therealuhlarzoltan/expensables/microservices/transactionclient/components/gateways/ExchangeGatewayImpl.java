package hu.therealuhlarzoltan.expensables.microservices.transactionclient.components.gateways;

import com.fasterxml.jackson.databind.ObjectMapper;
import hu.therealuhlarzoltan.expensables.api.microservices.core.exchange.ExchangeRequest;
import hu.therealuhlarzoltan.expensables.api.microservices.core.exchange.ExchangeResponse;
import hu.therealuhlarzoltan.expensables.api.microservices.core.transaction.TransactionRecord;
import hu.therealuhlarzoltan.expensables.api.microservices.exceptions.InvalidInputDataException;
import hu.therealuhlarzoltan.expensables.api.microservices.exceptions.NotFoundException;
import hu.therealuhlarzoltan.expensables.api.microservices.exceptions.ServiceResponseException;
import hu.therealuhlarzoltan.expensables.util.HttpErrorInfo;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.util.concurrent.TimeoutException;

import static java.util.logging.Level.FINE;

@Component
@RequiredArgsConstructor
public class ExchangeGatewayImpl implements ExchangeGateway {
    private final Logger LOG = LoggerFactory.getLogger(ExchangeGatewayImpl.class);
    @Value("${app.exchange-service-url}")
    private String EXCHANGE_SERVICE_URL;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

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
        return getPostForSingleReactive(url, requestBody, ExchangeResponse.class);
    }

    private <B, R> Mono<R> getPostForSingleReactive(URI url, B body, Class<R> clazz) {
        return webClient.post().uri(url)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(clazz)
                .log(LOG.getName(), FINE)
                .onErrorMap(Throwable.class, ex -> handleWebClientException(ex));
    }


    //Handling timeouts
    public Mono<ExchangeResponse> handleTimeoutFallback(String fromCurrency, String toCurrency, BigDecimal amount, TimeoutException ex) {
        throw new ServiceResponseException("Dependent service call failed", HttpStatus.FAILED_DEPENDENCY);
    }

    public Mono<ExchangeResponse> handleFallback(String fromCurrency, String toCurrency, BigDecimal amount, Throwable ex) {
        //Only handling 5xx server errors here
        if (ex instanceof WebClientResponseException && ((WebClientResponseException) ex).getStatusCode().is5xxServerError()) {
            return Mono.error(new ServiceResponseException("Service unavailable", HttpStatus.SERVICE_UNAVAILABLE));
        }
        //"Re-throwing" the exception if it's not a 5xx error
        return Mono.error(ex);
    }

    private <T> Mono<T> getForSingleReactive(URI url, Class<T> clazz) {
        return webClient.get().uri(url)
                .retrieve().bodyToMono(clazz)
                .log(LOG.getName(), FINE)
                .onErrorMap(Throwable.class, ex -> handleWebClientException(ex));
    }


    private Throwable handleWebClientException(Throwable ex) {

        if (!(ex instanceof WebClientResponseException) && !(ex instanceof TimeoutException)) {
            LOG.warn("Got a unexpected error: {}, will rethrow it", ex.toString());
            return ex;
        }
        if (ex instanceof TimeoutException) {
            //Time limiter handling this
            return ex;
        }

        WebClientResponseException wcre = (WebClientResponseException)ex;

        if (wcre.getStatusCode().is5xxServerError()) {
            //Circuit breaker handling this
            return ex;
        }

        //Resolving error
        switch (HttpStatus.resolve(wcre.getStatusCode().value())) {
            case NOT_FOUND:
                return new NotFoundException(getErrorMessage(wcre));
            case BAD_REQUEST, UNPROCESSABLE_ENTITY:
                return new InvalidInputDataException(getErrorMessage(wcre));

            default:
                LOG.warn("Got an unexpected HTTP error: {}, will rethrow it", wcre.getStatusCode());
                LOG.warn("Error body: {}", wcre.getResponseBodyAsString());
                return ex;
        }
    }

    private String getErrorMessage(WebClientResponseException ex) {
        try {
            return objectMapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        } catch (IOException ioex) {
            return ex.getMessage();
        }
    }
}
