package hu.therealuhlarzoltan.expensables.microservices.transactionclient.services;

import hu.therealuhlarzoltan.expensables.api.microservices.core.account.Account;
import hu.therealuhlarzoltan.expensables.api.microservices.core.exchange.ExchangeResponse;
import hu.therealuhlarzoltan.expensables.api.microservices.core.transaction.TransactionRecord;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface TransactionIntegration {
    Mono<TransactionRecord> getTransaction(String transactionId);
    Mono<TransactionRecord> createTransaction(TransactionRecord transaction);
    Mono<TransactionRecord> createTransactionWithExchange(TransactionRecord transaction);
    Mono<TransactionRecord> updateTransaction(TransactionRecord transaction, BigDecimal amount);
    Mono<TransactionRecord> updateTransactionWithExchange(TransactionRecord transaction, BigDecimal amount);
    Mono<Void> deleteTransaction(TransactionRecord transaction);
    Mono<Void> deleteTransactionWithExchange(TransactionRecord transaction);
    Mono<Account> getAccount(String accountId);
    Mono<Account> getAccountWithFallback(String accountId);
    Flux<TransactionRecord> getOutgoingTransactions(String accountId);
    Flux<TransactionRecord> getIncomingTransactions(String accountId);
    Mono<Void> depositToAccount(String accountId, BigDecimal amount);
    Mono<Void> withdrawFromAccount(String accountId, BigDecimal amount);

    Mono<String> getAccountCurrency(String toAccountId);
}
