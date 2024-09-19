package hu.therealuhlarzoltan.expensables.microservices.cashflow.components.gateways;

import hu.therealuhlarzoltan.expensables.api.microservices.core.account.Account;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface AccountGateway {
    Mono<Account> getAccount(String accountId);
    Mono<Account> getAccountWithFallback(String accountId);
    Mono<String> getAccountCurrency(String accountId);
    Mono<BigDecimal> getAccountBalance(String accountId);
}
