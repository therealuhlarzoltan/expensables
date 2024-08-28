package hu.therealuhlarzoltan.expensables.microservices.expense.controllers;

import hu.therealuhlarzoltan.expensables.api.microservices.core.expense.ExpenseController;
import hu.therealuhlarzoltan.expensables.api.microservices.core.expense.ExpenseRecord;
import hu.therealuhlarzoltan.expensables.microservices.expense.components.mappers.ExpenseRecordMapper;
import hu.therealuhlarzoltan.expensables.microservices.expense.services.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class ExpenseControllerImpl implements ExpenseController {
    private static final Logger LOG = LoggerFactory.getLogger(ExpenseControllerImpl.class);

    private final ExpenseRecordMapper mapper;
    private final ExpenseService service;

    @Override
    public Flux<ExpenseRecord> getAllExpenses() {
        LOG.info("Received a GET request for all expenses");
        return service.getAllExpenses();
    }

    @Override
    public Mono<ExpenseRecord> getExpense(@PathVariable String recordId) {
        LOG.info("Received a GET request for expense with id: {}", recordId);
       return service.getExpense(recordId);
    }

    @Override
    public Flux<ExpenseRecord> getExpensesByAccount(String accountId) {
        LOG.info("Received a GET request for expenses with account id: {}", accountId);
        return service.getExpensesByAccount(accountId);
    }

    @Override
    public Mono<ExpenseRecord> createExpense(ExpenseRecord expenseRecord) {
        LOG.info("Received a POST request to create an expense with request body: {}", expenseRecord);
        var entity = mapper.expenseRecordToEntity(expenseRecord);
        return service.createExpense(entity);
    }

    @Override
    public Mono<ExpenseRecord> updateExpense(ExpenseRecord expenseRecord) {
        LOG.info("Received a PUT request to update an expense with request body: {}", expenseRecord);
        var entity = mapper.expenseRecordToEntity(expenseRecord);
        return service.updateExpense(entity);
    }

    @Override
    public Mono<Void> deleteExpense(String recordId) {
        LOG.info("Received a DELETE request to delete an expense with id: {}", recordId);
        return service.deleteExpense(recordId);
    }
}
