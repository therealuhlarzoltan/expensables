package hu.therealuhlarzoltan.expensables.microservices.cashflow.controllers;

import hu.therealuhlarzoltan.expensables.api.microservices.composite.cashflow.CashflowClientController;
import hu.therealuhlarzoltan.expensables.api.microservices.composite.cashflow.ExpenseRecordInfo;
import hu.therealuhlarzoltan.expensables.api.microservices.composite.cashflow.IncomeRecordInfo;
import hu.therealuhlarzoltan.expensables.microservices.cashflow.services.CashflowService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class CashflowClientControllerImpl implements CashflowClientController {
    private static final Logger LOG = LoggerFactory.getLogger(CashflowClientControllerImpl.class);
    private final CashflowService service;

    @Override
    public Flux<IncomeRecordInfo> getAccountIncomes(String accountId) {
        LOG.info("Received a GET request to retrieve incomes for account with id: {}", accountId);
        return service.getAccountIncomes(accountId);
    }

    @Override
    public Flux<ExpenseRecordInfo> getAccountExpenses(String accountId) {
        LOG.info("Received a GET request to retrieve expenses for account with id: {}", accountId);
        return service.getAccountExpenses(accountId);
    }

    @Override
    public Mono<IncomeRecordInfo> getIncome(String incomeId) {
        LOG.info("Received a GET request to retrieve income with id: {}", incomeId);
        return service.getIncome(incomeId);
    }

    @Override
    public Mono<ExpenseRecordInfo> getExpense(String expenseId) {
        LOG.info("Received a GET request to retrieve expense with id: {}", expenseId);
        return service.getExpense(expenseId);
    }

    @Override
    public Mono<IncomeRecordInfo> createIncome(IncomeRecordInfo incomeRecordInfo) {
        LOG.info("Received a POST request to create income: {}", incomeRecordInfo);
        return service.createIncome(incomeRecordInfo);
    }

    @Override
    public Mono<ExpenseRecordInfo> createExpense(ExpenseRecordInfo expenseRecordInfo) {
        LOG.info("Received a POST request to create expense: {}", expenseRecordInfo);
        return service.createExpense(expenseRecordInfo);
    }

    @Override
    public Mono<IncomeRecordInfo> updateIncome(String incomeId, IncomeRecordInfo incomeRecordInfo) {
        LOG.info("Received a PUT request to update income with id: {}", incomeId);
        return service.updateIncome(incomeId, incomeRecordInfo);
    }

    @Override
    public Mono<ExpenseRecordInfo> updateExpense(String expenseId, ExpenseRecordInfo expenseRecordInfo) {
        LOG.info("Received a PUT request to update expense with id: {}", expenseId);
        return service.updateExpense(expenseId, expenseRecordInfo);
    }

    @Override
    public Mono<Void> deleteIncome(String incomeId) {
        LOG.info("Received a DELETE request to delete income with id: {}", incomeId);
        return service.deleteIncome(incomeId);
    }

    @Override
    public Mono<Void> deleteExpense(String expenseId) {
        LOG.info("Received a DELETE request to delete expense with id: {}", expenseId);
        return service.deleteExpense(expenseId);
    }
}
