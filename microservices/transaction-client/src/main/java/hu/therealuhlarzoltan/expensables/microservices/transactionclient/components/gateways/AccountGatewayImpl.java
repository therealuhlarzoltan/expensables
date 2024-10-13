package hu.therealuhlarzoltan.expensables.microservices.transactionclient.components.gateways;

import com.fasterxml.jackson.databind.ObjectMapper;
import hu.therealuhlarzoltan.expensables.api.microservices.core.account.Account;
import hu.therealuhlarzoltan.expensables.api.microservices.core.transaction.TransactionRecord;
import hu.therealuhlarzoltan.expensables.api.microservices.exceptions.InvalidInputDataException;
import hu.therealuhlarzoltan.expensables.api.microservices.exceptions.NotFoundException;
import hu.therealuhlarzoltan.expensables.api.microservices.exceptions.ServiceResponseException;
import hu.therealuhlarzoltan.expensables.util.HttpErrorInfo;
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
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.util.concurrent.TimeoutException;

import static hu.therealuhlarzoltan.expensables.microservices.transactionclient.components.gateways.WebClientRequests.*;

@Component
@RequiredArgsConstructor
public class AccountGatewayImpl implements AccountGateway {
    private final Logger LOG = LoggerFactory.getLogger(AccountGatewayImpl.class);
    @Value("${app.account-service-url}")
    private String ACCOUNT_SERVICE_URL;

    @CircuitBreaker(name = "accountService", fallbackMethod = "handleFallback")
    @Retry(name = "accountService")
    @TimeLimiter(name = "accountService", fallbackMethod = "handleTimeoutFallback")
    @Override
    public Mono<Account> getAccount(String accountId) {
        URI url = UriComponentsBuilder
                .fromUriString(ACCOUNT_SERVICE_URL + "/api/accounts/{accountId}")
                .build(accountId);
        return getForSingleReactive(url, Account.class);
    }

    @CircuitBreaker(name = "accountService", fallbackMethod = "handleFallback")
    @Retry(name = "accountService")
    @TimeLimiter(name = "accountService", fallbackMethod = "handleTimeoutFallback")
    @Override
    public Mono<String> getAccountCurrency(String accountId) {
        URI url = UriComponentsBuilder
                .fromUriString(ACCOUNT_SERVICE_URL + "/api/accounts/{accountId}")
                .build(accountId);
        return getForSingleReactive(url, Account.class)
                .map(account -> account.getCurrency());
    }

    @CircuitBreaker(name = "accountService", fallbackMethod = "handleFallback")
    @Retry(name = "accountService")
    @TimeLimiter(name = "accountService", fallbackMethod = "handleTimeoutFallback")
    @Override
    public Mono<BigDecimal> getAccountBalance(String accountId) {
        URI url = UriComponentsBuilder
                .fromUriString(ACCOUNT_SERVICE_URL + "/api/accounts/{accountId}")
                .build(accountId);
        return getForSingleReactive(url, Account.class)
                .map(account -> account.getBalance());
    }

    @CircuitBreaker(name = "accountService", fallbackMethod = "handleFallbackWithDefault")
    @Retry(name = "accountService")
    @TimeLimiter(name = "accountService", fallbackMethod = "handleTimeoutFallbackWithDefault")
    @Override
    public Mono<Account> getAccountWithFallback(String accountId) {
        URI url = UriComponentsBuilder
                .fromUriString(ACCOUNT_SERVICE_URL + "/api/accounts/{accountId}")
                .build(accountId);
        return getForSingleReactive(url, Account.class);
    }

    // Handling timeouts
    public Mono<Account> handleTimeoutFallback(String accountId, TimeoutException ex) {
        return Mono.error(new ServiceResponseException("Dependent service call failed", HttpStatus.FAILED_DEPENDENCY));
    }

    // Handling timeouts with default account
    public Mono<Account> handleTimeoutFallbackWithDefault(String accountId, TimeoutException ex) {
        return Mono.just(
                Account.builder()
                        .accountName("Unknown")
                        .accountId("Unknown").build());
    }

    // Handling exceptions
    public Mono<Account> handleFallback(String accountId, Throwable ex) {
        // Only handling 5xx server errors here
        if (ex instanceof WebClientResponseException && ((WebClientResponseException) ex).getStatusCode().is5xxServerError()) {
            return Mono.error(new ServiceResponseException("Dependent service call failed", HttpStatus.FAILED_DEPENDENCY));
        } else if (ex instanceof CallNotPermittedException) {
            return Mono.error(new ServiceResponseException("Service unavailable", HttpStatus.SERVICE_UNAVAILABLE));
        }
        // "Re-throwing" the exception if it's not a 5xx error
        return Mono.error(ex);
    }

    // Handling exceptions with default account
    public Mono<Account> handleFallbackWithDefault(String accountId, Throwable ex) {
        // Only handling 5xx server errors here
        if (ex instanceof WebClientResponseException && ((WebClientResponseException) ex).getStatusCode().is5xxServerError()) {
            return Mono.just(
                    Account.builder()
                            .accountName("Unknown")
                            .accountId("Unknown").build());
        } else if (ex instanceof CallNotPermittedException) {
            return Mono.just(
                    Account.builder()
                            .accountName("Unknown")
                            .accountId("Unknown").build());
        }
        // "Re-throwing" the exception if it's not a 5xx error
        return Mono.error(ex);
    }

}


