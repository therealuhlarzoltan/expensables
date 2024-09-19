package hu.therealuhlarzoltan.expensables.microservices.accountclient.components.gateways;

import hu.therealuhlarzoltan.expensables.api.microservices.core.expense.ExpenseRecord;
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
public class ExpenseGatewayImpl implements ExpenseGateway {
    @Value("${app.expense-service-url}")
    private String EXPENSE_SERVICE_URL;

    @CircuitBreaker(name = "expenseService", fallbackMethod = "handleFallback")
    @TimeLimiter(name = "expenseService", fallbackMethod = "handleTimeoutFallback")
    @Retry(name = "expenseService")
    @Override
    public Mono<ExpenseRecord> getExpense(String id) {
        URI url = UriComponentsBuilder
                .fromUriString(EXPENSE_SERVICE_URL + "/api/expenses/{recordId}")
                .build(id);
        return getForSingleReactive(url, ExpenseRecord.class);
    }

    @CircuitBreaker(name = "expenseService", fallbackMethod = "handleFallbackSingleDefault")
    @TimeLimiter(name = "expenseService", fallbackMethod = "handleTimeoutFallbackSingleDefault")
    @Retry(name = "expenseService")
    @Override
    public Mono<ExpenseRecord> getExpenseWithFallback(String id) {
        URI url = UriComponentsBuilder
                .fromUriString(EXPENSE_SERVICE_URL + "/api/expenses/{recordId}")
                .build(id);
        return getForSingleReactive(url, ExpenseRecord.class);
    }

    @CircuitBreaker(name = "expenseService", fallbackMethod = "handleFallbackMany")
    @TimeLimiter(name = "expenseService", fallbackMethod = "handleTimeoutFallbackMany")
    @Retry(name = "expenseService")
    @Override
    public Flux<ExpenseRecord> getExpenses(String accountId) {
        URI url = UriComponentsBuilder
                .fromUriString(EXPENSE_SERVICE_URL + "/api/expenses")
                .queryParam("accountId", accountId)
                .build().toUri();
        return getForManyReactive(url, ExpenseRecord.class);
    }

    @CircuitBreaker(name = "expenseService", fallbackMethod = "handleFallbackManyDefault")
    @TimeLimiter(name = "expenseService", fallbackMethod = "handleTimeoutFallbackManyDefault")
    @Retry(name = "expenseService")
    @Override
    public Flux<ExpenseRecord> getExpensesWithFallback(String accountId) {
        URI url = UriComponentsBuilder
                .fromUriString(EXPENSE_SERVICE_URL + "/api/expenses")
                .queryParam("accountId", accountId)
                .build().toUri();
        return getForManyReactive(url, ExpenseRecord.class);
    }

    public Mono<ExpenseRecord> handleTimeoutFallback(String id, TimeoutException ex) {
        return Mono.error(new ServiceResponseException("Dependent service call failed", HttpStatus.FAILED_DEPENDENCY));
    }

    public Mono<ExpenseRecord> handleTimeoutFallbackSingleDefault(String expenseId, TimeoutException ex) {
        return Mono.fromCallable(() -> buildUnknownExpense(expenseId));
    }

    public Flux<ExpenseRecord> handleTimeoutFallbackMany(String accountId, TimeoutException ex) {
        return Flux.error(new ServiceResponseException("Dependent service call failed", HttpStatus.FAILED_DEPENDENCY));
    }

    public Flux<ExpenseRecord> handleTimeoutFallbackManyDefault(String expenseId, TimeoutException ex) {
        return Flux.empty();
    }

    // Handling exceptions
    public Mono<ExpenseRecord> handleFallback(String expenseId, Throwable ex) {
        // Only handling 5xx server errors here
        if (ex instanceof WebClientResponseException && ((WebClientResponseException) ex).getStatusCode().is5xxServerError()) {
            return Mono.error(new ServiceResponseException("Dependent service call failed", HttpStatus.FAILED_DEPENDENCY));
        } else if (ex instanceof CallNotPermittedException) {
            return Mono.error(new ServiceResponseException("Service unavailable", HttpStatus.SERVICE_UNAVAILABLE));
        }
        // "Re-throwing" the exception if it's not a 5xx error
        return Mono.error(ex);
    }

    public Flux<ExpenseRecord> handleFallbackMany(String accountId, Throwable ex) {
        // Only handling 5xx server errors here
        if (ex instanceof WebClientResponseException && ((WebClientResponseException) ex).getStatusCode().is5xxServerError()) {
            return Flux.error(new ServiceResponseException("Dependent service call failed", HttpStatus.FAILED_DEPENDENCY));
        } else if (ex instanceof CallNotPermittedException) {
            return Flux.error(new ServiceResponseException("Service unavailable", HttpStatus.SERVICE_UNAVAILABLE));
        }
        // "Re-throwing" the exception if it's not a 5xx error
        return Flux.error(ex);
    }

    // Handling exceptions with default expense record
    public Mono<ExpenseRecord> handleFallbackSingleDefault(String expenseId, Throwable ex) {
        // Only handling 5xx server errors here
        if (ex instanceof WebClientResponseException && ((WebClientResponseException) ex).getStatusCode().is5xxServerError()) {
            return Mono.fromCallable(() -> buildUnknownExpense(expenseId));
        } else if (ex instanceof CallNotPermittedException) {
            return Mono.fromCallable(() -> buildUnknownExpense(expenseId));
        }
        // "Re-throwing" the exception if it's not a 5xx error
        return Mono.error(ex);
    }

    public Flux<ExpenseRecord> handleFallbackManyDefault(String accountId, Throwable ex) {
        if (ex instanceof WebClientResponseException && ((WebClientResponseException) ex).getStatusCode().is5xxServerError()) {
            return Flux.empty();
        } else if (ex instanceof CallNotPermittedException) {
            return Flux.empty();
        }
        return Flux.error(ex);
    }

    private static ExpenseRecord buildUnknownExpense(String expenseId) {
        return ExpenseRecord.builder()
                .recordId(expenseId)
                .expenseCategory("Unknown")
                .expenseName("Unknown")
                .expenseSubCategory("Unknown")
                .currency("Unknown")
                .accountId("Uknown")
                .build();
    }
}
