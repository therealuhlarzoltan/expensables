package hu.therealuhlarzoltan.expensables.microservices.transactionclient.services;

import hu.therealuhlarzoltan.expensables.api.microservices.composite.transaction.TransactionInfo;
import hu.therealuhlarzoltan.expensables.api.microservices.composite.transaction.TransactionRequest;
import hu.therealuhlarzoltan.expensables.api.microservices.composite.transaction.TransactionUpdateRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface TransactionClientService {

    Mono<TransactionInfo> getTransaction(String transactionId);

    Flux<TransactionInfo> getOutgoingTransactions(UUID accountId);

    Flux<TransactionInfo> getIncomingTransaction(UUID accountId);

    Mono<TransactionInfo> createTransaction(TransactionRequest transactionRequest);

    Mono<TransactionInfo> updateTransaction(String transactionId, TransactionUpdateRequest updateRequest);

    Mono<Void> deleteTransaction(String transactionId);
}
