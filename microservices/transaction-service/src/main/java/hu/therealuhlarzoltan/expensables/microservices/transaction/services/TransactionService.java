package hu.therealuhlarzoltan.expensables.microservices.transaction.services;

import hu.therealuhlarzoltan.expensables.api.microservices.core.transaction.TransactionRecord;
import hu.therealuhlarzoltan.expensables.microservices.transaction.models.TransactionEntity;
import jakarta.validation.Valid;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface TransactionService {
    Flux<TransactionRecord> getAllTransactions();

    Flux<TransactionRecord> getTransActionsForAccount(UUID accountId, boolean toAccount);

    Mono<TransactionRecord> getTransaction(String recordId);

    Flux<TransactionRecord> getTransactionsByUser(Long userId);

    Mono<TransactionRecord> createTransaction(@Valid TransactionEntity entity);

    Mono<TransactionRecord> updateTransaction(@Valid TransactionEntity entity);

    Mono<Void> deleteTransaction(String recordId);
    Mono<Void> deleteTransactionsByAccount(String accountId);
}
