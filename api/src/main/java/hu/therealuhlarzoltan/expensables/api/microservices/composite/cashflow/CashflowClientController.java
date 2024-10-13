package hu.therealuhlarzoltan.expensables.api.microservices.composite.cashflow;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CashflowClientController {
    @GetMapping(value = "/api/incomes/account/{accountId}", produces = "application/json")
    Flux<IncomeRecordInfo> getAccountIncomes(@PathVariable String accountId);

    @GetMapping(value = "/api/expenses/account/{accountId}", produces = "application/json")
    Flux<ExpenseRecordInfo> getAccountExpenses(@PathVariable String accountId);

    @GetMapping(value = "/api/incomes/{incomeId}", produces = "application/json")
    Mono<IncomeRecordInfo> getIncome(@PathVariable String incomeId);

    @GetMapping(value = "/api/expenses/{expenseId}", produces = "application/json")
    Mono<ExpenseRecordInfo> getExpense(@PathVariable String expenseId);

    @PostMapping(value = "/api/incomes", produces = "application/json", consumes = "application/json")
    Mono<IncomeRecordInfo> createIncome(@RequestBody IncomeRecordInfo incomeRecordInfo);

    @PostMapping(value ="/api/expenses", produces = "application/json", consumes = "application/json")
    Mono<ExpenseRecordInfo> createExpense(@RequestBody ExpenseRecordInfo expenseRecordInfo);

    @PutMapping(value = "/api/incomes/{incomeId}", produces = "application/json", consumes = "application/json")
    Mono<IncomeRecordInfo> updateIncome(@PathVariable String incomeId, @RequestBody IncomeRecordInfo incomeRecordInfo);

    @PutMapping(value = "/api/expenses/{expenseId}", produces = "application/json", consumes = "application/json")
    Mono<ExpenseRecordInfo> updateExpense(@PathVariable String expenseId, @RequestBody ExpenseRecordInfo expenseRecordInfo);

    @DeleteMapping(value = "/api/incomes/{incomeId}", produces = "application/json")
    @ResponseStatus(HttpStatus.ACCEPTED)
    Mono<Void> deleteIncome(@PathVariable String incomeId);

    @DeleteMapping("/api/expenses/{expenseId}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    Mono<Void> deleteExpense(@PathVariable String expenseId);

}
