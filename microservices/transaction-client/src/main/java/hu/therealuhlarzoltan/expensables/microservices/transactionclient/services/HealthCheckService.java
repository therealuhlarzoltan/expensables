package hu.therealuhlarzoltan.expensables.microservices.transactionclient.services;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import reactor.core.publisher.Mono;

public interface HealthCheckService {
    Mono<Health> getTransactionHealth();
    Mono<Health> getAccountHealth();
    Mono<Health> getExchangeHealth();
}
