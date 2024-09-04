package hu.therealuhlarzoltan.expensables.microservices.transactionclient.services;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;

import static java.util.logging.Level.FINE;

@Service
@RequiredArgsConstructor
public class HealthCheckServiceImpl implements HealthCheckService {
    @Value("${app.transaction-service-url}")
    private String TRANSACTION_SERVICE_URL;
    @Value("${app.account-service-url}")
    private String ACCOUNT_SERVICE_URL;
    @Value("${app.exchange-service-url}")
    private String EXCHANGE_SERVICE_URL;
    @Value("${app.health-check-path}")
    private String HEALTH_CHECK_PATH;

    private static final Logger LOG = LoggerFactory.getLogger(HealthCheckServiceImpl.class);
    private final WebClient webClient;

    @Override
    public Mono<Health> getTransactionHealth() {
        URI url = URI.create(TRANSACTION_SERVICE_URL + HEALTH_CHECK_PATH);
        return checkHealth(url);
    }

    @Override
    public Mono<Health> getAccountHealth() {
        URI url = URI.create(ACCOUNT_SERVICE_URL + HEALTH_CHECK_PATH);
        return checkHealth(url);
    }

    @Override
    public Mono<Health> getExchangeHealth() {
        URI url = URI.create(EXCHANGE_SERVICE_URL + HEALTH_CHECK_PATH);
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
