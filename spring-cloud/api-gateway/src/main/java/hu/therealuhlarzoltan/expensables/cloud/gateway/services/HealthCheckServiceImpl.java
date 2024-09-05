package hu.therealuhlarzoltan.expensables.cloud.gateway.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;

import static java.util.logging.Level.FINE;

@Service
public class HealthCheckServiceImpl implements HealthCheckService {
    private static final Logger LOG = LoggerFactory.getLogger(HealthCheckServiceImpl.class);
    private final WebClient webClient;

    @Autowired
    public HealthCheckServiceImpl(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    @Value("${app.account-service-url}")
    private String ACCOUNT_SERVICE_URL;
    @Value("${app.expense-service-url}")
    private String EXPENSE_SERVICE_URL;
    @Value("${app.income-service-url}")
    private String INCOME_SERVICE_URL;
    @Value("${app.transaction-service-url}")
    private String TRANSACTION_SERVICE_URL;
    @Value("${app.exchange-service-url}")
    private String EXCHANGE_SERVICE_URL;
    @Value("${app.account-client-url}")
    private String ACCOUNT_CLIENT_URL;
    @Value("${app.cashflow-client-url}")
    private String CASHFLOW_CLIENT_URL;
    @Value("${app.transaction-client-url}")
    private String TRANSACTION_CLIENT_URL;
    @Value("${app.health-check-path}")
    private String HEALTH_CHECK_PATH;

    @Override
    public Mono<Health> getAccountServiceHealth() {
        URI url = URI.create(ACCOUNT_SERVICE_URL + HEALTH_CHECK_PATH);
        return checkHealth(url);
    }

    @Override
    public Mono<Health> getExpenseServiceHealth() {
        URI url = URI.create(EXPENSE_SERVICE_URL + HEALTH_CHECK_PATH);
        return checkHealth(url);
    }

    @Override
    public Mono<Health> getIncomeServiceHealth() {
        URI url = URI.create(INCOME_SERVICE_URL + HEALTH_CHECK_PATH);
        return checkHealth(url);
    }

    @Override
    public Mono<Health> getTransactionServiceHealth() {
        URI url = URI.create(TRANSACTION_SERVICE_URL + HEALTH_CHECK_PATH);
        return checkHealth(url);
    }

    @Override
    public Mono<Health> getExchangeServiceHealth() {
        URI url = URI.create(EXCHANGE_SERVICE_URL + HEALTH_CHECK_PATH);
        return checkHealth(url);
    }

    @Override
    public Mono<Health> getAccountClientHealth() {
        URI url = URI.create(ACCOUNT_CLIENT_URL + HEALTH_CHECK_PATH);
        return checkHealth(url);
    }

    @Override
    public Mono<Health> getCashflowClientHealth() {
        URI url = URI.create(CASHFLOW_CLIENT_URL + HEALTH_CHECK_PATH);
        return checkHealth(url);
    }

    @Override
    public Mono<Health> getTransactionClientHealth() {
        URI url = URI.create(TRANSACTION_CLIENT_URL + HEALTH_CHECK_PATH);
        return checkHealth(url);
    }

    private Mono<Health> checkHealth(URI url) {
        LOG.debug("Will call the Health API on URL: {}", url);
        return webClient.get().uri(url).retrieve().bodyToMono(String.class)
                .map(s -> new Health.Builder().up().build())
                .onErrorResume(ex -> Mono.just(new Health.Builder().down(ex).build()))
                .log(LOG.getName(), FINE);
    }

}
