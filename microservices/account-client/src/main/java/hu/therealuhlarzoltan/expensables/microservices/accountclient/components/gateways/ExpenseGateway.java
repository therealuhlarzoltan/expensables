package hu.therealuhlarzoltan.expensables.microservices.accountclient.components.gateways;

import hu.therealuhlarzoltan.expensables.api.microservices.core.expense.ExpenseRecord;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

public interface ExpenseGateway {
    Mono<ExpenseRecord> getExpense(String id);

    Mono<ExpenseRecord> getExpenseWithFallback(String id);

    Flux<ExpenseRecord> getExpenses(String accountId);
    Flux<ExpenseRecord> getExpensesWithFallback(String accountId);
}
