package hu.therealuhlarzoltan.expensables.microservices.cashflow.config;

import hu.therealuhlarzoltan.expensables.microservices.cashflow.services.HealthCheckService;
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
    public ReactiveHealthContributor coreServices() {
        final Map<String, ReactiveHealthIndicator> registry = new LinkedHashMap<>();
        registry.put("account-service", () -> healthService.getAccountHealth());
        registry.put("income-service", () -> healthService.getIncomeHealth());
        registry.put("expense-service", () -> healthService.getExpenseHealth());
        registry.put("exchange-service", () -> healthService.getExchangeHealth());
        return CompositeReactiveHealthContributor.fromMap(registry);
    }
}
