package hu.therealuhlarzoltan.expensables.microservices.expense.services;

import com.mongodb.DuplicateKeyException;
import hu.therealuhlarzoltan.expensables.api.microservices.core.expense.ExpenseRecord;
import hu.therealuhlarzoltan.expensables.api.microservices.exceptions.InvalidInputDataException;
import hu.therealuhlarzoltan.expensables.api.microservices.exceptions.NotFoundException;
import hu.therealuhlarzoltan.expensables.microservices.expense.components.mappers.ExpenseRecordMapper;
import hu.therealuhlarzoltan.expensables.microservices.expense.components.validators.SameExpenseCategoryValidator;
import hu.therealuhlarzoltan.expensables.microservices.expense.models.ExpenseCategory;
import hu.therealuhlarzoltan.expensables.microservices.expense.models.ExpenseRecordEntity;
import hu.therealuhlarzoltan.expensables.microservices.expense.repositories.ExpenseRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.LocalDateTime;

@Service
@Validated
@RequiredArgsConstructor
public class ExpenseServiceImpl  implements ExpenseService {
    private static final Logger LOG = LoggerFactory.getLogger(ExpenseServiceImpl.class);

    private final ExpenseRecordMapper mapper;
    private final SameExpenseCategoryValidator categoryValidator;
    private final ExpenseRepository expenseRepository;

    @Override
    public Flux<ExpenseRecord> getAllExpenses() {
        LOG.info("Will retrieve all expense records");
        return expenseRepository.findAll().map(mapper::entityToExpenseRecord);
    }

    @Override
    public Mono<ExpenseRecord> getExpense(String recordId) {
        LOG.info("Will retrieve expense record with id: {}", recordId);
        return expenseRepository.findById(recordId)
                .switchIfEmpty(Mono.error(new NotFoundException("No expense record found for id: " + recordId)))
                .map(mapper::entityToExpenseRecord);
    }

    @Override
    public Flux<ExpenseRecord> getExpensesByAccount(String accountId) {
        LOG.info("Will retrieve all expense records for account with id: {}", accountId);
        return expenseRepository.findAllByAccountId(accountId).map(mapper::entityToExpenseRecord);
    }

    @Override
    public Mono<ExpenseRecord> createExpense(@Valid ExpenseRecordEntity expenseRecord) {
        LOG.info("Will create an expense record with validated data: {}", expenseRecord);
        if (expenseRecord.getId() != null && expenseRecord.getId().length() != 24)
            throw new InvalidInputDataException("Id must be 24 characters long");
        if (!categoryValidator.isValid(ExpenseCategory.valueOf(expenseRecord.getCategory()), expenseRecord.getSubCategory()))
            return Mono.error(new InvalidInputDataException("Expense category and subcategory must be the same"));
        expenseRecord.setVersion(null); //handled by mongo
        expenseRecord.setTimestamp(LocalDateTime.now());
        return expenseRepository.save(expenseRecord)
                .onErrorMap(DuplicateKeyException.class,
                        e -> new InvalidInputDataException("Record with id: " + expenseRecord.getId() + " already exists"))
                .map(mapper::entityToExpenseRecord);
    }

    @Override
    public Mono<ExpenseRecord> updateExpense(@Valid ExpenseRecordEntity expenseRecord) {
        LOG.info("Will update an expense record with validated data: {}", expenseRecord);
        if (!categoryValidator.isValid(ExpenseCategory.valueOf(expenseRecord.getCategory()), expenseRecord.getSubCategory()))
            return Mono.error(new InvalidInputDataException("Expense category and subcategory must be the same"));
        return expenseRepository.findById(expenseRecord.getId())
                .switchIfEmpty(Mono.error(new NotFoundException("No expense record found for id: " + expenseRecord.getId())))
                .flatMap(existingRecord -> {
                    existingRecord.setAccountId(expenseRecord.getAccountId());
                    existingRecord.setName(expenseRecord.getName());
                    existingRecord.setCurrency(expenseRecord.getCurrency());
                    existingRecord.setAmount(expenseRecord.getAmount());
                    existingRecord.setCategory(expenseRecord.getCategory());
                    existingRecord.setSubCategory(expenseRecord.getSubCategory());
                    return expenseRepository.save(existingRecord);
                })
                .map(mapper::entityToExpenseRecord);
    }

    @Override
    public Mono<Void> deleteExpense(String recordId) {
        LOG.info("Will delete expense record with id: {}", recordId);
        return expenseRepository.deleteById(recordId);
    }

}
