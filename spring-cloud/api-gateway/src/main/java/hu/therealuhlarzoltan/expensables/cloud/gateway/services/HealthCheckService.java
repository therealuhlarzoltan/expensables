package hu.therealuhlarzoltan.expensables.cloud.gateway.services;

import org.springframework.boot.actuate.health.Health;
import reactor.core.publisher.Mono;

public interface HealthCheckService {
    Mono<Health> getAccountServiceHealth();
    Mono<Health> getExpenseServiceHealth();
    Mono<Health> getIncomeServiceHealth();
    Mono<Health> getTransactionServiceHealth();
    Mono<Health> getExchangeServiceHealth();
    Mono<Health> getAccountClientHealth();
    Mono<Health> getCashflowClientHealth();
    Mono<Health> getTransactionClientHealth();
}
