package hu.therealuhlarzoltan.expensables.microservices.cashflow.components.gateways;

import hu.therealuhlarzoltan.expensables.api.microservices.core.expense.ExpenseRecord;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ExpenseGateway {
    Mono<ExpenseRecord> getExpense(String id);

    Mono<ExpenseRecord> getExpenseWithFallback(String id);

    Flux<ExpenseRecord> getExpenses(String accountId);
    Flux<ExpenseRecord> getExpensesWithFallback(String accountId);
}
