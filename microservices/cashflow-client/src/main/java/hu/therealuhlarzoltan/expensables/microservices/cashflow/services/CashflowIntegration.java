package hu.therealuhlarzoltan.expensables.microservices.cashflow.services;

import hu.therealuhlarzoltan.expensables.api.microservices.core.expense.ExpenseRecord;
import hu.therealuhlarzoltan.expensables.api.microservices.core.income.IncomeRecord;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CashflowIntegration {

    Mono<Void> deleteExpense(ExpenseRecord expenseRecord);
    Mono<Void> deleteIncome(IncomeRecord incomeRecord);

    Flux<IncomeRecord> getAccountIncomes(String accountId);

    Flux<ExpenseRecord> getAccountExpenses(String accountId);

    Mono<IncomeRecord> getIncome(String incomeId);

    Mono<ExpenseRecord> getExpense(String expenseId);

    Mono<Void> deleteExpenseWithExchange(ExpenseRecord expenseRecord, String targetCurrency);

    Mono<String> getAccountCurrency(String accountId);

    Mono<IncomeRecord> createIncome(IncomeRecord incomeRecord);
    Mono<IncomeRecord> createIncomeWithExchange(IncomeRecord incomeRecord, String targetCurrency);

    Mono<Void> deleteIncomeWithExchange(IncomeRecord incomeRecord, String targetCurrency);
    Mono<IncomeRecord> updateIncome(IncomeRecord incomeRecord);
    Mono<IncomeRecord> updateIncomeWithExchange(IncomeRecord incomeRecord, String targetCurrency);

    Mono<ExpenseRecord> updateExpenseWithExchange(ExpenseRecord expenseRecord, String targetCurrency);

    Mono<ExpenseRecord> updateExpense(ExpenseRecord expenseRecord);

    Mono<ExpenseRecord> createExpense(ExpenseRecord expenseRecord);

    Mono<ExpenseRecord> createExpenseWithExchange(ExpenseRecord expenseRecord, String targetCurrency);
}
