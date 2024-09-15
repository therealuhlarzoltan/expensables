package hu.therealuhlarzoltan.expensables.microservices.transactionclient.services;

import hu.therealuhlarzoltan.expensables.api.microservices.events.ResponsePayload;
import hu.therealuhlarzoltan.expensables.api.microservices.exceptions.ServiceResponseException;
import hu.therealuhlarzoltan.expensables.microservices.transactionclient.components.gateways.AccountGateway;
import hu.therealuhlarzoltan.expensables.microservices.transactionclient.components.gateways.ExchangeGateway;
import hu.therealuhlarzoltan.expensables.microservices.transactionclient.components.gateways.TransactionGateway;
import org.springframework.messaging.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import hu.therealuhlarzoltan.expensables.api.microservices.core.account.Account;
import hu.therealuhlarzoltan.expensables.api.microservices.core.exchange.ExchangeResponse;
import hu.therealuhlarzoltan.expensables.api.microservices.core.transaction.TransactionRecord;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import org.springframework.cloud.stream.function.StreamBridge;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

import static java.util.logging.Level.FINE;

@Service
public class TransactionIntegrationImpl implements TransactionIntegration {
    private static final Logger LOG = LoggerFactory.getLogger(TransactionIntegrationImpl.class);
    @Value("${app.transaction-service-url}")
    private String TRANSACTION_SERVICE_URL;
    @Value("${app.account-service-url}")
    private String ACCOUNT_SERVICE_URL;
    @Value("${app.exchange-service-url}")
    private String EXCHANGE_SERVICE_URL;

    private final AccountGateway accountGateway;
    private final ExchangeGateway exchangeGateway;
    private final TransactionGateway transactionGateway;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    private final StreamBridge streamBridge;

    private final Scheduler publishEventScheduler;

    @Autowired
    public TransactionIntegrationImpl(
            @Qualifier("publishEventScheduler") Scheduler publishEventScheduler,
            WebClient webClient,
            ObjectMapper mapper,
            StreamBridge streamBridge,
            AccountGateway accountGateway,
            ExchangeGateway exchangeGateway,
            TransactionGateway transactionGateway) {
        this.publishEventScheduler = publishEventScheduler;
        this.webClient = webClient;
        this.objectMapper = mapper;
        this.streamBridge = streamBridge;
        this.accountGateway = accountGateway;
        this.exchangeGateway = exchangeGateway;
        this.transactionGateway = transactionGateway;
    }

    @Override
    public Mono<TransactionRecord> createTransaction(TransactionRecord transaction) {
        return null;
    }

    @Override
    public Mono<TransactionRecord> getTransaction(String transactionId) {
        LOG.info("Will delegate the getTransaction API call to the TransactionGateway");
        return transactionGateway.getTransaction(transactionId);
    }

    @Override
    public Flux<TransactionRecord> getOutgoingTransactions(String accountId) {
        LOG.info("Will delegate the getOutgoingTransactions API call to the TransactionGateway");
        return transactionGateway.getOutgoingTransactions(accountId);
    }

    @Override
    public Flux<TransactionRecord> getIncomingTransactions(String accountId) {
        LOG.info("Will delegate the getIncomingTransactions API call to the TransactionGateway");
        return transactionGateway.getIncomingTransactions(accountId);
    }

    @Override
    public Mono<Void> deleteTransaction(String transactionId) {
        return null;
    }

    @Override
    public Mono<ExchangeResponse> exchangeCurrency(String fromCurrency, String toCurrency, BigDecimal amount) {
        LOG.debug("Will delegate the exchangeCurrency API call to the ExchangeGateway");
        return exchangeGateway.makeExchange(fromCurrency, toCurrency, amount);
    }

    @Override
    public Mono<Account> getAccount(String accountId) {
        LOG.info("Will delegate the getAccount API call to the AccountGateway");
        return accountGateway.getAccount(accountId);
    }

    @Override
    public Mono<Account> getAccountWithFallback(String accountId) {
        LOG.info("Will delegate the getAccountWithFallback API call to the AccountGateway");
        return accountGateway.getAccountWithFallback(accountId);
    }

    @Override
    public Mono<Void> depositToAccount(String accountId, BigDecimal amount) {
      return null;
    }

    @Override
    public Mono<Void> withdrawFromAccount(String accountId, BigDecimal amount) {
      return null;
    }

    @Override
    public Mono<String> getAccountCurrency(String accountId) {
        return  accountGateway.getAccountCurrency(accountId);
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

    private <T> Mono<T> getForSingleReactive(URI url, Class<T> clazz, Duration timeout) {
        return webClient.get().uri(url)
                .retrieve().bodyToMono(clazz)
                .timeout(timeout)
                .log(LOG.getName(), FINE)
                .onErrorMap(Throwable.class, ex -> handleWebClientException(ex));
    }

    private <T> Flux<T> getForManyReactive(URI url, Class<T> clazz, Duration timeout) {
        return webClient.get().uri(url)
                .retrieve().bodyToFlux(clazz)
                .timeout(timeout)
                .log(LOG.getName(), FINE)
                .onErrorMap(Throwable.class, ex -> handleWebClientException(ex));
    }

    private <T> T deserializeObjectFromJson(String json, Class<T> clazz) {
        T obj = null;
        try {
            obj = objectMapper.readValue(json, clazz);
        } catch (IOException e) {
            LOG.error("Couldn't deserialize object from json: {}", e.getMessage());
        }
        return obj;
    }

    private Throwable createMessageResponseError(ResponsePayload data) {
        return new ServiceResponseException(data.getMessage(), data.getStatus());
    }

    private Throwable handleWebClientException(Throwable ex) {

        if (!(ex instanceof WebClientResponseException) && !(ex instanceof TimeoutException)) {
            LOG.warn("Got a unexpected error: {}, will rethrow it", ex.toString());
            return ex;
        }
        if (ex instanceof TimeoutException) {
            return new ServiceResponseException("Dependent service call failed", HttpStatus.FAILED_DEPENDENCY);
        }

        WebClientResponseException wcre = (WebClientResponseException)ex;

        if (wcre.getStatusCode().is5xxServerError()) {
            return new ServiceResponseException("Dependent service call failed", HttpStatus.FAILED_DEPENDENCY);
        }

        switch (HttpStatus.resolve(wcre.getStatusCode().value())) {
            case NOT_FOUND:
                return new NotFoundException(getErrorMessage(wcre));
            case BAD_REQUEST, UNPROCESSABLE_ENTITY:
                return new InvalidInputDataException(getErrorMessage(wcre));

            default:
                LOG.warn("Got an unexpected HTTP error: {}, will rethrow it", wcre.getStatusCode());
                LOG.warn("Error body: {}", wcre.getResponseBodyAsString());
                return ex;
        }
    }

    private String getErrorMessage(WebClientResponseException ex) {
        try {
            return objectMapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        } catch (IOException ioex) {
            return ex.getMessage();
        }
    }
}
