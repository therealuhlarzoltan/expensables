package hu.therealuhlarzoltan.expensables.microservices.cashflow.components.gateways;

import hu.therealuhlarzoltan.expensables.api.microservices.core.income.IncomeRecord;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IncomeGateway {
    @CircuitBreaker(name = "incomeService", fallbackMethod = "handleFallback")
    @Retry(name = "incomeService")
    @TimeLimiter(name = "incomeService", fallbackMethod = "handleTimeoutFallback")
    Mono<IncomeRecord> getIncome(String id);

    @CircuitBreaker(name = "incomeService", fallbackMethod = "handleFallbackSingleDefault")
    @Retry(name = "incomeService")
    @TimeLimiter(name = "incomeService", fallbackMethod = "handleTimeoutFallbackSingleDefault")
    Mono<IncomeRecord> getIncomeWithFallback(String id);

    @CircuitBreaker(name = "incomeService", fallbackMethod = "handleFallbackMany")
    @Retry(name = "incomeService")
    @TimeLimiter(name = "incomeService", fallbackMethod = "handleTimeoutFallbackMany")
    Flux<IncomeRecord> getIncomes(String accountId);

    @CircuitBreaker(name = "incomeService", fallbackMethod = "handleFallbackManyDefault")
    @Retry(name = "incomeService")
    @TimeLimiter(name = "incomeService", fallbackMethod = "handleTimeoutFallbackManyDefault")
    Flux<IncomeRecord> getIncomesWithFallback(String accountId);
}
