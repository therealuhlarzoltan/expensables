package hu.therealuhlarzoltan.expensables.microservices.transactionclient.services;

import hu.therealuhlarzoltan.expensables.api.microservices.core.account.Account;
import hu.therealuhlarzoltan.expensables.api.microservices.core.exchange.ExchangeResponse;
import hu.therealuhlarzoltan.expensables.api.microservices.core.transaction.TransactionRecord;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface TransactionIntegration {
    Mono<TransactionRecord> createTransaction(TransactionRecord transaction);

    Mono<TransactionRecord> getTransaction(String transactionId);

    Mono<Void> deleteTransaction(String transactionId);

    Mono<ExchangeResponse> exchangeCurrency(String fromCurrency, String toCurrency, BigDecimal amount);

    Mono<Account> getAccount(String accountId);
    Mono<Account> getAccountWithFallback(String accountId);
    Flux<TransactionRecord> getOutgoingTransactions(String accountId);
    Flux<TransactionRecord> getIncomingTransactions(String accountId);

    Mono<Void> depositToAccount(String accountId, BigDecimal amount);

    Mono<Void> withdrawFromAccount(String accountId, BigDecimal amount);

    Mono<String> getAccountCurrency(String toAccountId);
}
