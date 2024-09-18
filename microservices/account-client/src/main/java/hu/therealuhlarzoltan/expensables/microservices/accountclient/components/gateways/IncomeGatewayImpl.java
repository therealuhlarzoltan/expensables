package hu.therealuhlarzoltan.expensables.microservices.accountclient.components.gateways;

import hu.therealuhlarzoltan.expensables.api.microservices.core.account.Account;
import hu.therealuhlarzoltan.expensables.api.microservices.core.income.IncomeRecord;
import hu.therealuhlarzoltan.expensables.api.microservices.exceptions.ServiceResponseException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.concurrent.TimeoutException;

import static hu.therealuhlarzoltan.expensables.microservices.accountclient.components.gateways.WebClientRequests.*;

@Component
@RequiredArgsConstructor
public class IncomeGatewayImpl implements IncomeGateway {
    @Value("${app.income-service-url}")
    private String INCOME_SERVICE_URL;

    @CircuitBreaker(name = "incomeService", fallbackMethod = "handleFallback")
    @Retry(name = "incomeService")
    @TimeLimiter(name = "incomeService", fallbackMethod = "handleTimeoutFallback")
    @Override
    public Mono<IncomeRecord> getIncome(String id) {
        URI url = UriComponentsBuilder
                .fromUriString(INCOME_SERVICE_URL + "/api/incomes/get/{recordId}")
                .build(id);
        return getForSingleReactive(url, IncomeRecord.class);
    }

    @CircuitBreaker(name = "incomeService", fallbackMethod = "handleFallbackSingleDefault")
    @Retry(name = "incomeService")
    @TimeLimiter(name = "incomeService", fallbackMethod = "handleTimeoutFallbackSingleDefault")
    @Override
    public Mono<IncomeRecord> getIncomeWithFallback(String id) {
        URI url = UriComponentsBuilder
                .fromUriString(INCOME_SERVICE_URL + "/api/incomes/get/{recordId}")
                .build(id);
        return getForSingleReactive(url, IncomeRecord.class);
    }

    @CircuitBreaker(name = "incomeService", fallbackMethod = "handleFallbackMany")
    @Retry(name = "incomeService")
    @TimeLimiter(name = "incomeService", fallbackMethod = "handleTimeoutFallbackMany")
    @Override
    public Flux<IncomeRecord> getIncomes(String accountId) {
        URI url = UriComponentsBuilder
                .fromUriString(INCOME_SERVICE_URL + "/api/incomes/{accountId}")
                .build(accountId);
        return getForManyReactive(url, IncomeRecord.class);
    }

    @CircuitBreaker(name = "incomeService", fallbackMethod = "handleFallbackManyDefault")
    @Retry(name = "incomeService")
    @TimeLimiter(name = "incomeService", fallbackMethod = "handleTimeoutFallbackManyDefault")
    @Override
    public Flux<IncomeRecord> getIncomesWithFallback(String accountId) {
        URI url = UriComponentsBuilder
                .fromUriString(INCOME_SERVICE_URL + "/api/incomes/{accountId}")
                .build(accountId);
        return getForManyReactive(url, IncomeRecord.class);
    }

    // Handle single fallbacks without defaults
    public Mono<IncomeRecord> handleTimeoutFallback(String id, TimeoutException ex) {
        return Mono.error(new ServiceResponseException("Dependent service call failed", HttpStatus.FAILED_DEPENDENCY));
    }

    public Mono<IncomeRecord> handleFallback(String id, Throwable ex) {
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
    public  Mono<IncomeRecord> handleTimeoutFallbackSingleDefault(String id, TimeoutException ex) {
        return Mono.fromCallable(() -> buildUnknownIncome(id));
    }

    public Mono<IncomeRecord> handleFallbackSingleDefault(String id, Throwable ex) {
        if (ex instanceof WebClientResponseException && ((WebClientResponseException) ex).getStatusCode().is5xxServerError()) {
            return Mono.fromCallable(() -> buildUnknownIncome(id));
        } else if (ex instanceof CallNotPermittedException) {
            return Mono.fromCallable(() -> buildUnknownIncome(id));
        }
        return Mono.error(ex);
    }

    // Handle many fallbacks without defaults
    public Flux<IncomeRecord> handleTimeoutFallbackMany(String accountId, TimeoutException ex) {
        return Flux.error(new ServiceResponseException("Dependent service call failed", HttpStatus.FAILED_DEPENDENCY));
    }


    public Flux<IncomeRecord> handleFallbackMany(String accountId, Throwable ex) {
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
    public  Flux<IncomeRecord> handleTimeoutFallbackManyDefault(String accountId, TimeoutException ex) {
        return Flux.empty();
    }

    public Flux<IncomeRecord> handleFallbackManyDefault(String accountId, Throwable ex) {
        if (ex instanceof WebClientResponseException && ((WebClientResponseException) ex).getStatusCode().is5xxServerError()) {
            return Flux.empty();
        } else if (ex instanceof CallNotPermittedException) {
            return Flux.empty();
        }
        return Flux.error(ex);
    }


    private static IncomeRecord buildUnknownIncome(String incomeId) {
        return IncomeRecord.builder()
                .recordId(incomeId)
                .accountId("Unknown")
                .incomeName("Unknown")
                .incomeCategory("Unknown")
                .currency("Unknown")
                .build();
    }
}
