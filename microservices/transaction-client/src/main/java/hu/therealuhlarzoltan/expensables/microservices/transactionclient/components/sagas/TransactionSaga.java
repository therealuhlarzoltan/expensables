package hu.therealuhlarzoltan.expensables.microservices.transactionclient.components.sagas;

import hu.therealuhlarzoltan.expensables.api.microservices.core.transaction.TransactionRecord;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface TransactionSaga {
    Mono<TransactionRecord> createTransaction(TransactionRecord transactionRecord);
    Mono<TransactionRecord> createTransaction(TransactionRecord transactionRecord, BigDecimal amount);
    Mono<TransactionRecord> updateTransaction(TransactionRecord transactionRecord, BigDecimal amount);
    Mono<TransactionRecord> updateTransaction(TransactionRecord transactionRecord, BigDecimal fromAmount, BigDecimal toAmount);
    Mono<Void> deleteTransaction(TransactionRecord transactionRecord);
    Mono<Void> deleteTransaction(TransactionRecord transactionRecord, BigDecimal amount);
}
