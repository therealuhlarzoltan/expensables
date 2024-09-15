package hu.therealuhlarzoltan.expensables.microservices.transactionclient.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import hu.therealuhlarzoltan.expensables.api.microservices.composite.transaction.TransactionInfo;
import hu.therealuhlarzoltan.expensables.api.microservices.composite.transaction.TransactionRequest;
import hu.therealuhlarzoltan.expensables.api.microservices.core.transaction.TransactionRecord;
import hu.therealuhlarzoltan.expensables.api.microservices.events.ResponsePayload;
import hu.therealuhlarzoltan.expensables.api.microservices.exceptions.InvalidInputDataException;
import hu.therealuhlarzoltan.expensables.api.microservices.exceptions.NotFoundException;
import hu.therealuhlarzoltan.expensables.api.microservices.exceptions.ServiceResponseException;
import hu.therealuhlarzoltan.expensables.microservices.transactionclient.components.mappers.TransactionMapper;
import hu.therealuhlarzoltan.expensables.util.HttpErrorInfo;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import static java.util.logging.Level.FINE;

@Service
@RequiredArgsConstructor
public class TransactionClientServiceImpl implements TransactionClientService {
    private static final Logger LOG = LoggerFactory.getLogger(TransactionClientServiceImpl.class);
    @Value("${app.transaction-service-url}")
    private String TRANSACTION_SERVICE_URL;
    @Value("${app.exchange-service-url}")
    private String EXCHANGE_SERVICE_URL;
    @Value("${app.account-service-url}")
    private String ACCOUNT_SERVICE_URL;
    private final TransactionIntegration integration;
    private final WebClient webClient;
    private final TransactionMapper transactionMapper;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<TransactionInfo> getTransaction(String transactionId) {
        LOG.info("Will call the getTransaction API in the integration layer with id: {}", transactionId);
        return integration.getTransaction(transactionId)
                .flatMap(transactionRecord -> {
                    LOG.info("Resolving account names for transaction: {}", transactionRecord);
                    Mono<String> fromAccountNameMono = integration.getAccountWithFallback(transactionRecord.getFromAccountId())
                            .map(account -> account.getAccountName());
                    Mono<String> toAccountNameMono = integration.getAccountWithFallback(transactionRecord.getToAccountId())
                            .map(account -> account.getAccountName());

                    return Mono.zip(fromAccountNameMono, toAccountNameMono)
                            .map(tuple -> {
                                LOG.info("Resolved accounts {} and {} for transaction with id {}", tuple.getT1(), tuple.getT2(), transactionId);
                                var transactionInfo = transactionMapper.transactionRecordToInfo(transactionRecord);
                                transactionInfo.setFromAccountName(tuple.getT1());
                                transactionInfo.setToAccountName(tuple.getT2());
                                return transactionInfo;
                            });
                });
    }

    @Override
    public Flux<TransactionInfo> getOutgoingTransactions(UUID accountId) {
        LOG.info("Will call the getOutgoingTransactions API in the integration layer with id: {}", accountId);
        return integration.getOutgoingTransactions(accountId.toString())
                .flatMap(transactionRecord -> {
            Mono<String> fromAccountNameMono = integration.getAccountWithFallback(transactionRecord.getFromAccountId())
                    .map(account -> account.getAccountName());
            Mono<String> toAccountNameMono = integration.getAccountWithFallback(transactionRecord.getToAccountId())
                    .map(account -> account.getAccountName());

            return Mono.zip(fromAccountNameMono, toAccountNameMono)
                    .map(tuple -> {
                        var transactionInfo = transactionMapper.transactionRecordToInfo(transactionRecord);
                        transactionInfo.setFromAccountName(tuple.getT1());
                        transactionInfo.setToAccountName(tuple.getT2());
                        return transactionInfo;
                    });
        });
    }

    @Override
    public Flux<TransactionInfo> getIncomingTransaction(UUID accountId) {
        LOG.info("Will call the getIncomingTransactions API in the integration layer with id: {}", accountId);
        return integration.getIncomingTransactions(accountId.toString())
                .flatMap(transactionRecord -> {
                    Mono<String> fromAccountNameMono = integration.getAccountWithFallback(transactionRecord.getFromAccountId())
                            .map(account -> account.getAccountName());
                    Mono<String> toAccountNameMono = integration.getAccountWithFallback(transactionRecord.getToAccountId())
                            .map(account -> account.getAccountName());

                    return Mono.zip(fromAccountNameMono, toAccountNameMono)
                            .map(tuple -> {
                                var transactionInfo = transactionMapper.transactionRecordToInfo(transactionRecord);
                                transactionInfo.setFromAccountName(tuple.getT1());
                                transactionInfo.setToAccountName(tuple.getT2());
                                return transactionInfo;
                            });
                });
    }

    @Override
    public Mono<TransactionInfo> createTransaction(TransactionRequest transactionRequest) {
        LOG.info("Will call the integration layer to create transaction with body: {}", transactionRequest);
        Mono<String> fromAccountCurrencyMono = integration.getAccountCurrency(transactionRequest.getFromAccountId());
        Mono<String> toAccountCurrencyMono = integration.getAccountCurrency(transactionRequest.getToAccountId());

        return Mono.zip(fromAccountCurrencyMono, toAccountCurrencyMono)
                .map(tuple -> TransactionInfo.builder()
                        .transactionId("test")
                        .fromAccountName("dummy")
                        .toAccountName("dummy")
                        .fromCurrency(tuple.getT1())
                        .toCurrency(tuple.getT2())
                        .build());
    }

    @Override
    public Mono<TransactionInfo> updateTransaction(String transactionId, TransactionRequest transactionRequest) {
        LOG.info("Will call the integration layer to update transaction with body: {}", transactionRequest);
        return null;
    }

    @Override
    public Mono<Void> deleteTransaction(String transactionId) {
        LOG.info("Will call the integration layer to delete transaction with id: {}", transactionId);
        return integration.deleteTransaction(transactionId);
    }

}
