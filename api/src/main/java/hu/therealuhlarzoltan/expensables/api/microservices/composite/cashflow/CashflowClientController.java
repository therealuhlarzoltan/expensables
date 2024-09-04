package hu.therealuhlarzoltan.expensables.api.microservices.composite.cashflow;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

public interface CashflowClientController {
    @GetMapping("/api/incomes/account/{accountId}")
    Mono<IncomeRecordInfo> getAccountIncomes(@PathVariable String accountId);

    @GetMapping("/api/expenses/account/{accountId}")
    Mono<ExpenseRecordInfo> getAccountExpenses(@PathVariable String accountId);

    @GetMapping("/api/incomes/{incomeId}")
    Mono<IncomeRecordInfo> getIncome(@PathVariable String incomeId);

    @GetMapping("/api/expenses/{expenseId}")
    Mono<ExpenseRecordInfo> getExpense(@PathVariable String expenseId);

    @PostMapping("/api/incomes")
    Mono<IncomeRecordInfo> createIncome(@RequestBody IncomeRecordInfo incomeRecordInfo);

    @PostMapping("/api/expenses")
    Mono<ExpenseRecordInfo> createExpense(@RequestBody ExpenseRecordInfo expenseRecordInfo);

    @PutMapping("/api/incomes/{incomeId}")
    Mono<IncomeRecordInfo> updateIncome(@PathVariable String incomeId, @RequestBody IncomeRecordInfo incomeRecordInfo);

    @PutMapping("/api/expenses/{expenseId}")
    Mono<ExpenseRecordInfo> updateExpense(@PathVariable String expenseId, @RequestBody ExpenseRecordInfo expenseRecordInfo);

    @DeleteMapping("/api/incomes/{incomeId}")
    Mono<Void> deleteIncome(@PathVariable String incomeId);

    @DeleteMapping("/api/expenses/{expenseId}")
    Mono<Void> deleteExpense(@PathVariable String expenseId);

}
