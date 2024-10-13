package hu.therealuhlarzoltan.expensables.microservices.cashflow.services;

import hu.therealuhlarzoltan.expensables.api.microservices.composite.cashflow.ExpenseRecordInfo;
import hu.therealuhlarzoltan.expensables.api.microservices.composite.cashflow.IncomeRecordInfo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CashflowService {
    Flux<IncomeRecordInfo> getAccountIncomes(String accountId);

    Flux<ExpenseRecordInfo> getAccountExpenses(String accountId);

    Mono<IncomeRecordInfo> getIncome(String incomeId);

    Mono<ExpenseRecordInfo> getExpense(String expenseId);

    Mono<IncomeRecordInfo> createIncome(IncomeRecordInfo incomeRecordInfo);

    Mono<ExpenseRecordInfo> createExpense(ExpenseRecordInfo expenseRecordInfo);

    Mono<IncomeRecordInfo> updateIncome(String incomeId, IncomeRecordInfo incomeRecordInfo);

    Mono<ExpenseRecordInfo> updateExpense(String expenseId, ExpenseRecordInfo expenseRecordInfo);

    Mono<Void> deleteIncome(String incomeId);

    Mono<Void> deleteExpense(String expenseId);
}
