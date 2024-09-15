package hu.therealuhlarzoltan.expensables.microservices.transactionclient.components.gateways;

import com.fasterxml.jackson.databind.ObjectMapper;
import hu.therealuhlarzoltan.expensables.api.microservices.core.transaction.TransactionRecord;
import hu.therealuhlarzoltan.expensables.api.microservices.events.ResponsePayload;
import hu.therealuhlarzoltan.expensables.api.microservices.exceptions.InvalidInputDataException;
import hu.therealuhlarzoltan.expensables.api.microservices.exceptions.NotFoundException;
import hu.therealuhlarzoltan.expensables.api.microservices.exceptions.ServiceResponseException;
import hu.therealuhlarzoltan.expensables.util.HttpErrorInfo;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

import static java.util.logging.Level.FINE;

@Component
@RequiredArgsConstructor
public class TransactionGatewayImpl implements TransactionGateway {
    private final Logger LOG = LoggerFactory.getLogger(TransactionGatewayImpl.class);
    @Value("${app.transaction-service-url}")
    private String TRANSACTION_SERVICE_URL;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Retry(name = "transactionService")
    @TimeLimiter(name = "transactionService", fallbackMethod = "handleTimeoutFallback")
    @CircuitBreaker(name = "transactionService", fallbackMethod = "handleFallback")
    @Override
    public Mono<TransactionRecord> getTransaction(String transactionId) {
        URI url = UriComponentsBuilder
                .fromUriString(TRANSACTION_SERVICE_URL + "/api/transactions/{recordId}")
                .build(transactionId);
        return getForSingleReactive(url, TransactionRecord.class);
    }

    @Retry(name = "transactionService")
    @TimeLimiter(name = "transactionService", fallbackMethod = "handleTimeoutFallback")
    @CircuitBreaker(name = "transactionService", fallbackMethod = "handleFallback")
    @Override
    public Flux<TransactionRecord> getAccountTransactions(String accountId) {
        URI url = UriComponentsBuilder
                .fromUriString(TRANSACTION_SERVICE_URL + "/api/transactions")
                .queryParam("accountId", accountId)
                .build().toUri();
        return getForManyReactive(url, TransactionRecord.class);
    }

    @Retry(name = "transactionService")
    @TimeLimiter(name = "transactionService", fallbackMethod = "handleTimeoutFallback")
    @CircuitBreaker(name = "transactionService", fallbackMethod = "handleFallback")
    @Override
    public Flux<TransactionRecord> getOutgoingTransactions(String accountId) {
        URI url = UriComponentsBuilder
                .fromUriString(TRANSACTION_SERVICE_URL + "/api/transactions")
                .queryParam("accountId", accountId)
                .queryParam("destination", "from")
                .build().toUri();
        return getForManyReactive(url, TransactionRecord.class);
    }

    @Retry(name = "transactionService")
    @TimeLimiter(name = "transactionService", fallbackMethod = "handleTimeoutFallback")
    @CircuitBreaker(name = "transactionService", fallbackMethod = "handleFallback")
    @Override
    public Flux<TransactionRecord> getIncomingTransactions(String accountId) {
        URI url = UriComponentsBuilder
                .fromUriString(TRANSACTION_SERVICE_URL + "/api/transactions")
                .queryParam("accountId", accountId)
                .queryParam("destination", "to")
                .build().toUri();
        return getForManyReactive(url, TransactionRecord.class);
    }

    //Handling timeouts
    public Mono<TransactionRecord> handleTimeoutFallback(String transactionId, TimeoutException ex) {
        throw new ServiceResponseException("Dependent service call failed", HttpStatus.FAILED_DEPENDENCY);
    }

    public Mono<TransactionRecord> handleFallback(String transactionId, Throwable ex) {
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

    private <T> Flux<T> getForManyReactive(URI url, Class<T> clazz) {
        return webClient.get().uri(url)
                .retrieve().bodyToFlux(clazz)
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
