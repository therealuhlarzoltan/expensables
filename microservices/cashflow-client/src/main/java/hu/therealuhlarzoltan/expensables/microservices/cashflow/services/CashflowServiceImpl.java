package hu.therealuhlarzoltan.expensables.microservices.cashflow.services;

import hu.therealuhlarzoltan.expensables.api.microservices.composite.cashflow.ExpenseRecordInfo;
import hu.therealuhlarzoltan.expensables.api.microservices.composite.cashflow.IncomeRecordInfo;
import hu.therealuhlarzoltan.expensables.api.microservices.core.income.IncomeRecord;
import hu.therealuhlarzoltan.expensables.microservices.cashflow.components.mappers.ExpenseMapper;
import hu.therealuhlarzoltan.expensables.microservices.cashflow.components.mappers.IncomeMapper;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CashflowServiceImpl implements CashflowService {
    private static final Logger LOG = LoggerFactory.getLogger(CashflowServiceImpl.class);
    private final IncomeMapper incomeMapper;
    private final ExpenseMapper expenseMapper;
    private final CashflowIntegration integration;
    private final ResponseListenerServiceImpl responseListener;


    @Override
    public Flux<IncomeRecordInfo> getAccountIncomes(String accountId) {
        LOG.info("Will call the getAccountIncomes API in the integration layer with accountId: {}", accountId);
        return integration.getAccountIncomes(accountId)
                .map(incomeMapper::incomeRecordToInfo);
    }

    @Override
    public Flux<ExpenseRecordInfo> getAccountExpenses(String accountId) {
        LOG.info("Will call the getAccountExpenses API in the integration layer with accountId: {}", accountId);
        return integration.getAccountExpenses(accountId).map(expenseMapper::expenseRecordToInfo);
    }

    @Override
    public Mono<IncomeRecordInfo> getIncome(String incomeId) {
        LOG.info("Will call the getIncome API in the integration layer with id: {}", incomeId);
        return integration.getIncome(incomeId)
                .map(incomeMapper::incomeRecordToInfo);
    }

    @Override
    public Mono<ExpenseRecordInfo> getExpense(String expenseId) {
        LOG.info("Will call the getExpense API in the integration layer with id: {}", expenseId);
        return integration.getExpense(expenseId)
                .map(expenseMapper::expenseRecordToInfo);
    }

    @Override
    public Mono<IncomeRecordInfo> createIncome(IncomeRecordInfo incomeRecordInfo) {
        incomeRecordInfo.setRecordId(new ObjectId().toHexString());
        LOG.info("Will call the integration layer to create income: {}", incomeRecordInfo);
        return integration.getAccountCurrency(incomeRecordInfo.getAccountId())
                .flatMap(targetCurrency -> {
                    if (targetCurrency.equals(incomeRecordInfo.getCurrency())) {
                        return integration.createIncome(incomeMapper.incomeInfoToRecord(incomeRecordInfo))
                                .map(incomeMapper::incomeRecordToInfo);
                    } else {
                        return integration.createIncomeWithExchange(incomeMapper.incomeInfoToRecord(incomeRecordInfo), targetCurrency)
                                .map(incomeMapper::incomeRecordToInfo);
                    }
                });
    }

    @Override
    public Mono<ExpenseRecordInfo> createExpense(ExpenseRecordInfo expenseRecordInfo) {
        expenseRecordInfo.setRecordId(new ObjectId().toHexString());
        LOG.info("Will call the integration layer to create expense: {}", expenseRecordInfo);
        return integration.getAccountCurrency(expenseRecordInfo.getAccountId())
                .flatMap(targetCurrency -> {
                    if (targetCurrency.equals(expenseRecordInfo.getCurrency())) {
                        return integration.createExpense(expenseMapper.expenseInfoToRecord(expenseRecordInfo))
                                .map(expenseMapper::expenseRecordToInfo);
                    } else {
                        return integration.createExpenseWithExchange(expenseMapper.expenseInfoToRecord(expenseRecordInfo), targetCurrency)
                                .map(expenseMapper::expenseRecordToInfo);
                    }
                });
    }

    @Override
    public Mono<IncomeRecordInfo> updateIncome(String incomeId, IncomeRecordInfo incomeRecordInfo) {
        LOG.info("Will call the integration layer to update income with id: {} and body: {}", incomeId, incomeRecordInfo);
        return integration.getIncome(incomeId)
                .doOnError(ex -> {
                    LOG.error("Income not found with id: {}", incomeId);
                })
                .flatMap(rec -> integration.getAccountCurrency(rec.getAccountId())
                        .doOnError(ex -> {
                            LOG.error("Error while getting account currency for account id: {}, exception: {}", rec.getAccountId(), ex.getMessage());
                        })
                        .flatMap(targetCurrency -> {
                            if (targetCurrency.equals(rec.getCurrency())) {
                                return integration.updateIncome(incomeMapper.incomeInfoToRecord(incomeRecordInfo))
                                        .map(incomeMapper::incomeRecordToInfo);
                            } else {
                                return integration.updateIncomeWithExchange(incomeMapper.incomeInfoToRecord(incomeRecordInfo), targetCurrency)
                                        .map(incomeMapper::incomeRecordToInfo);
                            }
                        })
                );
    }

    @Override
    public Mono<ExpenseRecordInfo> updateExpense(String expenseId, ExpenseRecordInfo expenseRecordInfo) {
        LOG.info("Will call the integration layer to update expense with id: {} and body: {}", expenseId, expenseRecordInfo);
        return integration.getExpense(expenseId)
                .doOnError(ex -> {
                    LOG.error("Expense not found with id: {}", expenseId);
                })
                .flatMap(rec -> integration.getAccountCurrency(rec.getAccountId())
                        .doOnError(ex -> {
                            LOG.error("Error while getting account currency for account id: {}, exception: {}", rec.getAccountId(), ex.getMessage());
                        })
                        .flatMap(targetCurrency -> {
                            if (targetCurrency.equals(rec.getCurrency())) {
                                return integration.updateExpense(expenseMapper.expenseInfoToRecord(expenseRecordInfo))
                                        .map(expenseMapper::expenseRecordToInfo);
                            } else {
                                return integration.updateExpenseWithExchange(expenseMapper.expenseInfoToRecord(expenseRecordInfo), targetCurrency)
                                        .map(expenseMapper::expenseRecordToInfo);
                            }
                        })
                );
    }

    @Override
    public Mono<Void> deleteIncome(String incomeId) {
        LOG.info("Will call the integration layer to delete income with id: {}", incomeId);
        return integration.getIncome(incomeId)
                .doOnError(ex -> {
                    LOG.error("Income not found with id: {}", incomeId);
                })
                .flatMap(rec -> integration.getAccountCurrency(rec.getAccountId())
                        .doOnError(ex -> {
                            LOG.error("Error while getting account currency for account id: {}, exception: {}", rec.getAccountId(), ex.getMessage());
                        })
                        .flatMap(targetCurrency -> {
                            if (targetCurrency.equals(rec.getCurrency())) {
                                return integration.deleteIncome(rec);
                            } else {
                                return integration.deleteIncomeWithExchange(rec, targetCurrency);
                            }
                        })
                );
    }

    @Override
    public Mono<Void> deleteExpense(String expenseId) {
        LOG.info("Will call the integration layer to delete expense with id: {}", expenseId);
        return integration.getExpense(expenseId)
                .doOnError(ex -> {
                    LOG.error("Expense not found with id: {}", expenseId);
                })
                .flatMap(rec -> integration.getAccountCurrency(rec.getAccountId())
                        .doOnError(ex -> {
                            LOG.error("Error while getting account currency for account id: {}, exception: {}", rec.getAccountId(), ex.getMessage());
                        })
                        .flatMap(targetCurrency -> {
                            if (targetCurrency.equals(rec.getCurrency())) {
                                return integration.deleteExpense(rec);
                            } else {
                                return integration.deleteExpenseWithExchange(rec, targetCurrency);
                            }
                        })
                );
    }
}
