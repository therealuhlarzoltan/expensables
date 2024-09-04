package hu.therealuhlarzoltan.expensables.microservices.transactionclient.services;

import org.springframework.messaging.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import hu.therealuhlarzoltan.expensables.api.microservices.core.account.Account;
import hu.therealuhlarzoltan.expensables.api.microservices.core.exchange.ExchangeRequest;
import hu.therealuhlarzoltan.expensables.api.microservices.core.exchange.ExchangeResponse;
import hu.therealuhlarzoltan.expensables.api.microservices.core.transaction.TransactionRecord;
import hu.therealuhlarzoltan.expensables.api.microservices.events.AccountEvent;
import hu.therealuhlarzoltan.expensables.api.microservices.events.CrudEvent;
import hu.therealuhlarzoltan.expensables.api.microservices.events.Event;
import hu.therealuhlarzoltan.expensables.api.microservices.exceptions.InvalidInputDataException;
import hu.therealuhlarzoltan.expensables.api.microservices.exceptions.NotFoundException;
import hu.therealuhlarzoltan.expensables.util.HttpErrorInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import org.springframework.cloud.stream.function.StreamBridge;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;

import static java.util.logging.Level.FINE;

@Service
public class TransactionIntegrationImpl implements TransactionIntegration {
    private static final Logger LOG = LoggerFactory.getLogger(TransactionIntegrationImpl.class);
    @Value("${app.transaction-service-url}")
    private static String TRANSACTION_SERVICE_URL;
    @Value("${app.account-service-url}")
    private static String ACCOUNT_SERVICE_URL;
    @Value("${app.exchange-service-url}")
    private static String EXCHANGE_SERVICE_URL;

    private final WebClient webClient;
    private final ObjectMapper mapper;

    private final StreamBridge streamBridge;

    private final Scheduler publishEventScheduler;

    @Autowired
    public TransactionIntegrationImpl(
            @Qualifier("publishEventScheduler") Scheduler publishEventScheduler,
            WebClient webClient,
            ObjectMapper mapper,
            StreamBridge streamBridge) {
        this.publishEventScheduler = publishEventScheduler;
        this.webClient = webClient;
        this.mapper = mapper;
        this.streamBridge = streamBridge;
    }

    @Override
    public Mono<TransactionRecord> createTransaction(TransactionRecord transaction) {
        return null;
    }

    @Override
    public Mono<TransactionRecord> getTransaction(String transactionId) {
        URI url = UriComponentsBuilder.fromUriString(TRANSACTION_SERVICE_URL + "/api/transactions/{transactionId}").build(transactionId);
        LOG.debug("Will call the getTransaction API on URL: {}", url);

        return webClient.get().uri(url)
                .retrieve().bodyToMono(TransactionRecord.class).log(LOG.getName(), FINE)
                .onErrorMap(WebClientResponseException.class, ex -> handleException(ex));
    }

    @Override
    public Mono<Void> deleteTransaction(String transactionId) {
        return null;
    }


    @Override
    public Mono<ExchangeResponse> exchangeCurrency(ExchangeRequest exchangeRequest) {
        URI url = URI.create(EXCHANGE_SERVICE_URL + "/api/exchange");
        LOG.debug("Will call the exchangeCurrency API on URL: {}", url);

        return webClient.post().uri(url).bodyValue(exchangeRequest)
                .retrieve().bodyToMono(ExchangeResponse.class).log(LOG.getName(), FINE)
                .onErrorMap(WebClientResponseException.class, ex -> handleException(ex));
    }

    @Override
    public Mono<Account> getAccount(String accountId) {
        URI url = UriComponentsBuilder.fromUriString(ACCOUNT_SERVICE_URL + "/api/accounts/{accountId}").build(accountId);
        LOG.debug("Will call the getTransaction API on URL: {}", url);

        return webClient.get().uri(url)
                .retrieve().bodyToMono(Account.class).log(LOG.getName(), FINE)
                .onErrorMap(WebClientResponseException.class, ex -> handleException(ex));
    }

    @Override
    public Mono<Void> depositToAccount(String accountId, BigDecimal amount) {
      return null;
    }

    @Override
    public Mono<Void> withdrawFromAccount(String accountId, BigDecimal amount) {
      return null;
    }


    private void sendMessage(String bindingName, Event<?, ?> event) {
        LOG.debug("Sending a {} message to {}", event.getEventType(), bindingName);
        Message message = MessageBuilder.withPayload(event)
                .setHeader("partitionKey", event.getKey())
                .build();
        streamBridge.send(bindingName, message);
    }

    private Throwable handleException(Throwable ex) {

        if (!(ex instanceof WebClientResponseException)) {
            LOG.warn("Got a unexpected error: {}, will rethrow it", ex.toString());
            return ex;
        }

        WebClientResponseException wcre = (WebClientResponseException)ex;

        switch (HttpStatus.resolve(wcre.getStatusCode().value())) {
            case NOT_FOUND:
                return new NotFoundException(getErrorMessage(wcre));
            case UNPROCESSABLE_ENTITY:
                return new InvalidInputDataException(getErrorMessage(wcre));

            default:
                LOG.warn("Got an unexpected HTTP error: {}, will rethrow it", wcre.getStatusCode());
                LOG.warn("Error body: {}", wcre.getResponseBodyAsString());
                return ex;
        }
    }

    private String getErrorMessage(WebClientResponseException ex) {
        try {
            return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        } catch (IOException ioex) {
            return ex.getMessage();
        }
    }
}
