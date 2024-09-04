package hu.therealuhlarzoltan.expensables.microservices.transactionclient.config;

import hu.therealuhlarzoltan.expensables.microservices.transactionclient.services.HealthCheckService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.CompositeReactiveHealthContributor;
import org.springframework.boot.actuate.health.ReactiveHealthContributor;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class HealthCheckConfig {
    private final HealthCheckService healthService;

    @Bean
    ReactiveHealthContributor coreServices() {
        final Map<String, ReactiveHealthIndicator> registry = new LinkedHashMap<>();
        registry.put("account", () -> healthService.getAccountHealth());
        registry.put("transaction", () -> healthService.getTransactionHealth());
        registry.put("exchange", () -> healthService.getExchangeHealth());

        return CompositeReactiveHealthContributor.fromMap(registry);
    }
}
