package hu.therealuhlarzoltan.expensables.microservices.exchange.config;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@Configuration
public class ApplicationConfig {
    @Value("${app.exchange-api-url}")
    private String EXTERNAL_API_URL;
    private final String EXTERNAL_API_KEY;

    @Autowired
    public ApplicationConfig(Dotenv dotenv) {
        EXTERNAL_API_KEY = dotenv.get("EXCHANGE_API_KEY");
    }

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder.baseUrl(EXTERNAL_API_URL)
                .defaultHeader("x-rapidapi-host", "currency-conversion-and-exchange-rates.p.rapidapi.com")
                .defaultHeader("x-rapidapi-key", EXTERNAL_API_KEY)
                .build();
    }
}
