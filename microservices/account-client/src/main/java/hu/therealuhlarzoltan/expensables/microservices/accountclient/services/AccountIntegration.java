package hu.therealuhlarzoltan.expensables.microservices.accountclient.services;

import hu.therealuhlarzoltan.expensables.api.microservices.composite.account.AccountInformation;
import hu.therealuhlarzoltan.expensables.api.microservices.composite.account.ExpenseInfo;
import hu.therealuhlarzoltan.expensables.api.microservices.core.account.Account;
import hu.therealuhlarzoltan.expensables.api.microservices.core.expense.ExpenseRecord;
import hu.therealuhlarzoltan.expensables.api.microservices.core.income.IncomeRecord;
import hu.therealuhlarzoltan.expensables.api.microservices.core.transaction.TransactionRecord;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

public interface AccountIntegration {
    Mono<Account> getAccount(String accountId);
    Mono<Account> createAccount(Account account);
    Mono<Account> updateAccount(Account account);
    Mono<Void> deleteAccount(Account account);
    Flux<IncomeRecord> getIncomes(String accountId);
    Flux<IncomeRecord> getIncomesWithFallback(String accountId);
    Flux<ExpenseRecord> getExpenses(String accountId);
    Flux<ExpenseRecord> getExpensesWithFallback(String accountId);

    Flux<TransactionRecord> getIncomingTransactions(String accountId);
    Flux<TransactionRecord> getIncomingTransactionsWithFallback(String accountId);
    Flux<TransactionRecord> getOutgoingTransactions(String accountId);
    Flux<TransactionRecord> getOutgoingTransactionsWithFallback(String accountId);

    Mono<Account> getAccountWithFallback(String fromAccountId);
}
