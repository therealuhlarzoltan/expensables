package hu.therealuhlarzoltan.expensables.microservices.transactionclient.services;

import hu.therealuhlarzoltan.expensables.api.microservices.events.ResponsePayload;
import hu.therealuhlarzoltan.expensables.api.microservices.exceptions.ServiceResponseException;
import hu.therealuhlarzoltan.expensables.microservices.transactionclient.components.gateways.AccountGateway;
import hu.therealuhlarzoltan.expensables.microservices.transactionclient.components.gateways.ExchangeGateway;
import hu.therealuhlarzoltan.expensables.microservices.transactionclient.components.gateways.TransactionGateway;
import hu.therealuhlarzoltan.expensables.microservices.transactionclient.components.sagas.TransactionSaga;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import hu.therealuhlarzoltan.expensables.api.microservices.core.account.Account;
import hu.therealuhlarzoltan.expensables.api.microservices.core.transaction.TransactionRecord;
import hu.therealuhlarzoltan.expensables.api.microservices.events.Event;
import hu.therealuhlarzoltan.expensables.api.microservices.exceptions.InvalidInputDataException;
import hu.therealuhlarzoltan.expensables.api.microservices.exceptions.NotFoundException;
import hu.therealuhlarzoltan.expensables.util.HttpErrorInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.cloud.stream.function.StreamBridge;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

import static java.util.logging.Level.FINE;

@Service
@RequiredArgsConstructor
public class TransactionIntegrationImpl implements TransactionIntegration {
    private static final Logger LOG = LoggerFactory.getLogger(TransactionIntegrationImpl.class);

    private final AccountGateway accountGateway;
    private final ExchangeGateway exchangeGateway;
    private final TransactionGateway transactionGateway;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    private final StreamBridge streamBridge;
    private final TransactionSaga transactionSaga;


    @Override
    public Mono<TransactionRecord> createTransaction(TransactionRecord transaction) {
        LOG.info("Will delegate the createTransaction API call to the TransactionSaga with id: {}", transaction.getRecordId());
        return transactionSaga.createTransaction(transaction)
                .doOnError(throwable -> LOG.error("Error while creating transaction: {}", throwable.getMessage()));
    }

    @Override
    public Mono<TransactionRecord> createTransactionWithExchange(TransactionRecord transaction) {
        LOG.info("Will delegate the createTransactionWithExchange API call to the TransactionSaga with id: {}", transaction.getRecordId());
        return exchangeGateway.makeExchange(transaction.getFromCurrency(), transaction.getToCurrency(), transaction.getAmount(), transaction.getTransactionDate())
                .flatMap(exchange -> transactionSaga.createTransaction(transaction, exchange.getResult()))
                .doOnError(throwable -> LOG.error("Error while creating transaction with exchange: {}", throwable.getMessage()));
    }

    @Override
    public Mono<TransactionRecord> updateTransaction(TransactionRecord transaction, BigDecimal amount) {
        LOG.info("Will delegate the updateTransaction API call to the TransactionSaga with id: {}", transaction.getRecordId());
        return transactionSaga.updateTransaction(transaction, amount.subtract(transaction.getAmount()))
                .doOnError(throwable -> LOG.error("Error while updating transaction: {}", throwable.getMessage()));
    }

    @Override
    public Mono<TransactionRecord> updateTransactionWithExchange(TransactionRecord transaction, BigDecimal amount) {
        LOG.info("Will delegate the updateTransactionWithExchange API call to the TransactionSaga with id: {}", transaction.getRecordId());
        return exchangeGateway.makeExchange(transaction.getFromCurrency(), transaction.getToCurrency(), amount.subtract(transaction.getAmount()).abs(), transaction.getTransactionDate())
                .flatMap(exchange -> transactionSaga.updateTransaction(transaction, amount.subtract(transaction.getAmount()),  amount.subtract(transaction.getAmount()).compareTo(BigDecimal.ZERO) > 0 ? exchange.getResult() : exchange.getResult().negate()))
                .doOnError(throwable -> LOG.error("Error while updating transaction with exchange: {}", throwable.getMessage()));
    }

    @Override
    public Mono<Void> deleteTransaction(TransactionRecord transaction) {
        LOG.info("Will delegate the deleteTransaction API call to the TransactionSaga with id: {}", transaction.getRecordId());
        return  transactionSaga.deleteTransaction(transaction)
                .doOnError(throwable -> LOG.error("Error while deleting transaction: {}", throwable.getMessage()));
    }

    @Override
    public Mono<Void> deleteTransactionWithExchange(TransactionRecord transaction) {
        LOG.info("Will delegate the deleteTransactionWithExchange API call to the TransactionSaga with id: {}", transaction.getRecordId());
        return exchangeGateway.makeExchange(transaction.getFromCurrency(), transaction.getToCurrency(), transaction.getAmount(), transaction.getTransactionDate())
                .flatMap(exchange -> transactionSaga.deleteTransaction(transaction, exchange.getResult()))
                .doOnError(throwable -> LOG.error("Error while deleting transaction with exchange: {}", throwable.getMessage()));
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
}
