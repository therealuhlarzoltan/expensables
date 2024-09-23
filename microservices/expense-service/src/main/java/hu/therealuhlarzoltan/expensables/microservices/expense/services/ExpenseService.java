package hu.therealuhlarzoltan.expensables.microservices.expense.services;

import hu.therealuhlarzoltan.expensables.api.microservices.core.expense.ExpenseRecord;
import hu.therealuhlarzoltan.expensables.microservices.expense.models.ExpenseRecordEntity;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Validated
public interface ExpenseService {
    Flux<ExpenseRecord> getAllExpenses();
    Mono<ExpenseRecord> getExpense(String recordId);
    Flux<ExpenseRecord> getExpensesByAccount(String accountId);
    Mono<ExpenseRecord> createExpense(@Valid ExpenseRecordEntity expenseRecord);
    Mono<ExpenseRecord> updateExpense(@Valid ExpenseRecordEntity expenseRecord);
    Mono<Void> deleteExpense(String recordId);
    Mono<Void> deleteExpensesByAccount(String accountId);
}
