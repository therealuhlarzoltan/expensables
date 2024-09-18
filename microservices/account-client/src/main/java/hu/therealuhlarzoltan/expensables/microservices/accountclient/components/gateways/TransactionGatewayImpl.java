package hu.therealuhlarzoltan.expensables.microservices.accountclient.components.gateways;

import hu.therealuhlarzoltan.expensables.api.microservices.core.transaction.TransactionRecord;
import hu.therealuhlarzoltan.expensables.api.microservices.exceptions.ServiceResponseException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.concurrent.TimeoutException;

import static hu.therealuhlarzoltan.expensables.microservices.accountclient.components.gateways.WebClientRequests.*;

@Component
@RequiredArgsConstructor
public class TransactionGatewayImpl implements TransactionGateway {
    @Value("${app.transaction-service-url}")
    private String TRANSACTION_SERVICE_URL;

    @CircuitBreaker(name = "transactionService", fallbackMethod = "handleFallbackSingle")
    @TimeLimiter(name = "transactionService", fallbackMethod = "handleTimeoutFallbackSingle")
    @Retry(name = "transactionService")
    @Override
    public Mono<TransactionRecord> getTransaction(String id) {
        URI url = UriComponentsBuilder
                .fromUriString(TRANSACTION_SERVICE_URL + "/api/transactions/{recordId}")
                .build(id);
        return getForSingleReactive(url, TransactionRecord.class);
    }

    @CircuitBreaker(name = "transactionService", fallbackMethod = "handleFallbackSingleDefault")
    @TimeLimiter(name = "transactionService", fallbackMethod = "handleTimeoutFallbackSingleDefault")
    @Retry(name = "transactionService")
    @Override
    public Mono<TransactionRecord> getTransactionWithFallback(String id) {
        URI url = UriComponentsBuilder
                .fromUriString(TRANSACTION_SERVICE_URL + "/api/transactions/{recordId}")
                .build(id);
        return getForSingleReactive(url, TransactionRecord.class);
    }

    @CircuitBreaker(name = "transactionService", fallbackMethod = "handleFallbackMany")
    @TimeLimiter(name = "transactionService", fallbackMethod = "handleTimeoutFallbackMany")
    @Retry(name = "transactionService")
    @Override
    public Flux<TransactionRecord> getTransactions(String accountId) {
        URI url = UriComponentsBuilder
                .fromUriString(TRANSACTION_SERVICE_URL + "/api/transactions")
                .queryParam("accountId", accountId)
                .build().toUri();
        return getForManyReactive(url, TransactionRecord.class);
    }

    @CircuitBreaker(name = "transactionService", fallbackMethod = "handleFallbackManyDefault")
    @TimeLimiter(name = "transactionService", fallbackMethod = "handleTimeoutFallbackManyDefault")
    @Retry(name = "transactionService")
    @Override
    public Flux<TransactionRecord> getTransactionsWithFallback(String accountId) {
        URI url = UriComponentsBuilder
                .fromUriString(TRANSACTION_SERVICE_URL + "/api/transactions")
                .queryParam("accountId", accountId)
                .build().toUri();
        return getForManyReactive(url, TransactionRecord.class);
    }

    @CircuitBreaker(name = "transactionService", fallbackMethod = "handleFallbackMany")
    @TimeLimiter(name = "transactionService", fallbackMethod = "handleTimeoutFallbackMany")
    @Retry(name = "transactionService")
    @Override
    public Flux<TransactionRecord> getIncomingTransactions(String accountId) {
        URI url = UriComponentsBuilder
                .fromUriString(TRANSACTION_SERVICE_URL + "/api/transactions")
                .queryParam("accountId", accountId)
                .queryParam("destination", "to")
                .build().toUri();
        return getForManyReactive(url, TransactionRecord.class);
    }

    @CircuitBreaker(name = "transactionService", fallbackMethod = "handleFallbackManyDefault")
    @TimeLimiter(name = "transactionService", fallbackMethod = "handleTimeoutFallbackManyDefault")
    @Retry(name = "transactionService")
    @Override
    public Flux<TransactionRecord> getIncomingTransactionsWithFallback(String accountId) {
        URI url = UriComponentsBuilder
                .fromUriString(TRANSACTION_SERVICE_URL + "/api/transactions")
                .queryParam("accountId", accountId)
                .queryParam("destination", "to")
                .build().toUri();
        return getForManyReactive(url, TransactionRecord.class);
    }

    @CircuitBreaker(name = "transactionService", fallbackMethod = "handleFallbackMany")
    @TimeLimiter(name = "transactionService", fallbackMethod = "handleTimeoutFallbackMany")
    @Retry(name = "transactionService")
    @Override
    public Flux<TransactionRecord> getOutgoingTransactions(String accountId) {
        URI url = UriComponentsBuilder
                .fromUriString(TRANSACTION_SERVICE_URL + "/api/transactions")
                .queryParam("accountId", accountId)
                .queryParam("destination", "from")
                .build().toUri();
        return getForManyReactive(url, TransactionRecord.class);
    }

    @CircuitBreaker(name = "transactionService", fallbackMethod = "handleFallbackManyDefault")
    @TimeLimiter(name = "transactionService", fallbackMethod = "handleTimeoutFallbackManyDefault")
    @Retry(name = "transactionService")
    @Override
    public Flux<TransactionRecord> getOutgoingTransactionsWithFallback(String accountId) {
        URI url = UriComponentsBuilder
                .fromUriString(TRANSACTION_SERVICE_URL + "/api/transactions")
                .queryParam("accountId", accountId)
                .queryParam("destination", "from")
                .build().toUri();
        return getForManyReactive(url, TransactionRecord.class);
    }

    // Handle single fallbacks without defaults
    public Mono<TransactionRecord> handleTimeoutFallbackSingle(String id, TimeoutException ex) {
        return Mono.error(new ServiceResponseException("Dependent service call failed", HttpStatus.FAILED_DEPENDENCY));
    }

    public Mono<TransactionRecord> handleFallbackSingle(String id, Throwable ex) {
        // Only handling 5xx server errors here
        if (ex instanceof WebClientResponseException && ((WebClientResponseException) ex).getStatusCode().is5xxServerError()) {
            return Mono.error(new ServiceResponseException("Service unavailable", HttpStatus.SERVICE_UNAVAILABLE));
        } else if (ex instanceof CallNotPermittedException) {
            return Mono.error(new ServiceResponseException("Service unavailable", HttpStatus.SERVICE_UNAVAILABLE));
        }
        // "Re-throwing" the exception if it's not a 5xx error
        return Mono.error(ex);
    }

    // Handle single fallbacks with defaults
    public  Mono<TransactionRecord> handleTimeoutFallbackSingleDefault(String id, TimeoutException ex) {
        return Mono.fromCallable(() -> buildUnknownTransaction(id));
    }

    public Mono<TransactionRecord> handleFallbackSingleDefault(String id, Throwable ex) {
        if (ex instanceof WebClientResponseException && ((WebClientResponseException) ex).getStatusCode().is5xxServerError()) {
            return Mono.fromCallable(() -> buildUnknownTransaction(id));
        } else if (ex instanceof CallNotPermittedException) {
            return Mono.fromCallable(() -> buildUnknownTransaction(id));
        }
        return Mono.error(ex);
    }

    // Handle many fallbacks without defaults
    public Flux<TransactionRecord> handleTimeoutFallbackMany(String id, TimeoutException ex) {
        return Flux.error(new ServiceResponseException("Dependent service call failed", HttpStatus.FAILED_DEPENDENCY));
    }

    public Flux<TransactionRecord> handleFallbackMany(String id, Throwable ex) {
        // Only handling 5xx server errors here
        if (ex instanceof WebClientResponseException && ((WebClientResponseException) ex).getStatusCode().is5xxServerError()) {
            return Flux.error(new ServiceResponseException("Service unavailable", HttpStatus.SERVICE_UNAVAILABLE));
        } else if (ex instanceof CallNotPermittedException) {
            return Flux.error(new ServiceResponseException("Service unavailable", HttpStatus.SERVICE_UNAVAILABLE));
        }
        // "Re-throwing" the exception if it's not a 5xx error
        return Flux.error(ex);
    }

    // Handle many fallbacks with defaults
    public Flux<TransactionRecord> handleTimeoutFallbackManyDefault(String accountId, TimeoutException ex) {
        return Flux.empty();
    }

    public Flux<TransactionRecord> handleTimeoutFallbackManyDefault(String accountId, Throwable ex) {
        if (ex instanceof WebClientResponseException && ((WebClientResponseException) ex).getStatusCode().is5xxServerError()) {
            return Flux.empty();
        } else if (ex instanceof CallNotPermittedException) {
            return Flux.empty();
        }
        return Flux.error(ex);
    }

    private static TransactionRecord buildUnknownTransaction(String recordId) {
        return TransactionRecord.builder()
                .recordId(recordId)
                .fromAccountId("Unknown")
                .toAccountId("Unknown")
                .fromCurrency("Unknown")
                .toCurrency("Unknown")
                .build();
    }
}
