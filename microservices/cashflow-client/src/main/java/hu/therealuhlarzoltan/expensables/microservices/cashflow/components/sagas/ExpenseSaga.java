package hu.therealuhlarzoltan.expensables.microservices.cashflow.components.sagas;

import hu.therealuhlarzoltan.expensables.api.microservices.core.expense.ExpenseRecord;
import hu.therealuhlarzoltan.expensables.api.microservices.core.income.IncomeRecord;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface ExpenseSaga {
    Mono<ExpenseRecord> createExpense(ExpenseRecord expenseRecord);
    Mono<ExpenseRecord> createExpense(ExpenseRecord expenseRecord, BigDecimal amount);
    Mono<ExpenseRecord> updateExpense(ExpenseRecord expenseRecord, BigDecimal amount);
    Mono<Void> deleteExpense(ExpenseRecord expenseRecord);
    Mono<Void> deleteExpense(ExpenseRecord expenseRecord, BigDecimal amount);
}
