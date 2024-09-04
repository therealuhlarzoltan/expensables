package hu.therealuhlarzoltan.expensables.microservices.transactionclient.controllers;

import hu.therealuhlarzoltan.expensables.api.microservices.composite.transaction.TransactionClientController;
import hu.therealuhlarzoltan.expensables.api.microservices.composite.transaction.TransactionInfo;
import hu.therealuhlarzoltan.expensables.api.microservices.composite.transaction.TransactionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

public class TransactionClientControllerImpl implements TransactionClientController {
    private static final Logger LOG = LoggerFactory.getLogger(TransactionClientControllerImpl.class);

    @Override
    public Mono<TransactionInfo> getTransactionInfo(String transactionId) {
        return null;
    }

    @Override
    public Mono<TransactionInfo> createTransaction(TransactionRequest transactionRequest) {
        return null;
    }

    @Override
    public Mono<TransactionInfo> updateTransaction(String transactionId, TransactionRequest transactionRequest) {
        return null;
    }

    @Override
    public Mono<Void> deleteTransaction(String transactionId) {
        return null;
    }
}
