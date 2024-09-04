package hu.therealuhlarzoltan.expensables.microservices.transactionclient.services;

import hu.therealuhlarzoltan.expensables.api.microservices.core.account.Account;
import hu.therealuhlarzoltan.expensables.api.microservices.core.exchange.ExchangeRequest;
import hu.therealuhlarzoltan.expensables.api.microservices.core.exchange.ExchangeResponse;
import hu.therealuhlarzoltan.expensables.api.microservices.core.transaction.TransactionRecord;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface TransactionIntegration {
    Mono<TransactionRecord> createTransaction(TransactionRecord transaction);

    Mono<TransactionRecord> getTransaction(String transactionId);

    Mono<Void> deleteTransaction(String transactionId);

    Mono<ExchangeResponse> exchangeCurrency(ExchangeRequest exchangeRequest);

    Mono<Account> getAccount(String accountId);

    Mono<Void> depositToAccount(String accountId, BigDecimal amount);

    Mono<Void> withdrawFromAccount(String accountId, BigDecimal amount);
}
