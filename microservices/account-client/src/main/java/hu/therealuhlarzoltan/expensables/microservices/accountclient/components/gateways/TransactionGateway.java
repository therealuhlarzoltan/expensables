package hu.therealuhlarzoltan.expensables.microservices.accountclient.components.gateways;

import hu.therealuhlarzoltan.expensables.api.microservices.core.transaction.TransactionRecord;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TransactionGateway {
    Mono<TransactionRecord> getTransaction(String id);
    Mono<TransactionRecord> getTransactionWithFallback(String id);
    Flux<TransactionRecord> getTransactions(String accountId);
    Flux<TransactionRecord> getTransactionsWithFallback(String accountId);
    Flux<TransactionRecord> getIncomingTransactions(String accountId);
    Flux<TransactionRecord> getIncomingTransactionsWithFallback(String accountId);
    Flux<TransactionRecord> getOutgoingTransactions(String accountId);
    Flux<TransactionRecord> getOutgoingTransactionsWithFallback(String accountId);
}
