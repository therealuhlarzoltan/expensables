package hu.therealuhlarzoltan.expensables.cloud.gateway.config;

import hu.therealuhlarzoltan.expensables.cloud.gateway.services.HealthCheckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.CompositeReactiveHealthContributor;
import org.springframework.boot.actuate.health.ReactiveHealthContributor;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class HealthCheckConfig {
    private final HealthCheckService healthService;

    @Autowired
    public HealthCheckConfig(HealthCheckService healthService) {
        this.healthService = healthService;
    }

    @Bean
    public ReactiveHealthContributor coreServices() {
        final Map<String, ReactiveHealthIndicator> registry = new LinkedHashMap<>();
        registry.put("account-service", () -> healthService.getAccountServiceHealth());
        registry.put("expense-service", () -> healthService.getExpenseServiceHealth());
        registry.put("income-service", () -> healthService.getIncomeServiceHealth());
        registry.put("transaction-service", () -> healthService.getTransactionServiceHealth());
        registry.put("exchange-service", () -> healthService.getExchangeServiceHealth());
        return CompositeReactiveHealthContributor.fromMap(registry);
    }

    @Bean
    public ReactiveHealthContributor compositeServices() {
        final Map<String, ReactiveHealthIndicator> registry = new LinkedHashMap<>();
        registry.put("account-client", () -> healthService.getAccountClientHealth());
        registry.put("cashflow-client", () -> healthService.getCashflowClientHealth());
        registry.put("transaction-client", () -> healthService.getTransactionClientHealth());
        return CompositeReactiveHealthContributor.fromMap(registry);
    }
}
