package hu.therealuhlarzoltan.expensables.microservices.accountclient.services;

import org.springframework.boot.actuate.health.Health;
import reactor.core.publisher.Mono;

public interface HealthCheckService {
    Mono<Health> getAccountHealth();
    Mono<Health> getIncomeHealth();
    Mono<Health> getExpenseHealth();
    Mono<Health> getTransactionHealth();
}
