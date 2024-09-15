package hu.therealuhlarzoltan.expensables.microservices.transactionclient.controllers;

import hu.therealuhlarzoltan.expensables.api.microservices.composite.transaction.TransactionClientController;
import hu.therealuhlarzoltan.expensables.api.microservices.composite.transaction.TransactionInfo;
import hu.therealuhlarzoltan.expensables.api.microservices.composite.transaction.TransactionRequest;
import hu.therealuhlarzoltan.expensables.microservices.transactionclient.services.TransactionClientService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class TransactionClientControllerImpl implements TransactionClientController {
    private static final Logger LOG = LoggerFactory.getLogger(TransactionClientControllerImpl.class);
    private final TransactionClientService service;

    @Override
    public Flux<TransactionInfo> getIncomingTransactions(@RequestParam UUID accountId) {
        LOG.info("Received a GET request for incoming transactions with accountId: {}", accountId.toString());
        return service.getIncomingTransaction(accountId);
    }

    @Override
    public Flux<TransactionInfo> getOutgoingTransactions(@RequestParam UUID accountId) {
        LOG.info("Received a GET request for outgoing transactions with accountId: {}", accountId.toString());
        return service.getOutgoingTransactions(accountId);
    }

    @Override
    public Mono<TransactionInfo> getTransactionInfo(String transactionId) {
        LOG.info("Received a GET request for transaction record with accountId");
        return service.getTransaction(transactionId);
    }

    @Override
    public Mono<TransactionInfo> createTransaction(TransactionRequest transactionRequest) {
        LOG.info("Received a POST request to create a transaction with body: {}", transactionRequest);
        return service.createTransaction(transactionRequest);
    }

    @Override
    public Mono<TransactionInfo> updateTransaction(String transactionId, TransactionRequest transactionRequest) {
        LOG.info("Received a PUT request to update a transaction with id: {} and body: {}", transactionId, transactionRequest);
        return service.updateTransaction(transactionId, transactionRequest);
    }

    @Override
    public Mono<Void> deleteTransaction(String transactionId) {
        LOG.info("Received a DELETE request to delete transaction with id: {}", transactionId);
        return service.deleteTransaction(transactionId);
    }
}
