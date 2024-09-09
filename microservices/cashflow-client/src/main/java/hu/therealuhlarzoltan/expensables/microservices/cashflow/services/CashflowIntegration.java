package hu.therealuhlarzoltan.expensables.microservices.cashflow.services;

import hu.therealuhlarzoltan.expensables.api.microservices.core.expense.ExpenseRecord;
import hu.therealuhlarzoltan.expensables.api.microservices.core.income.IncomeRecord;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CashflowIntegration {

    Mono<Void> deleteExpense(String expenseId);
    Mono<Void> deleteIncome(String incomeId);
}
