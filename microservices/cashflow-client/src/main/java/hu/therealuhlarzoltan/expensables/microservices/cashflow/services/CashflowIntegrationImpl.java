package hu.therealuhlarzoltan.expensables.microservices.cashflow.services;

import hu.therealuhlarzoltan.expensables.api.microservices.core.exchange.ExchangeResponse;
import hu.therealuhlarzoltan.expensables.api.microservices.core.expense.ExpenseRecord;
import hu.therealuhlarzoltan.expensables.api.microservices.core.income.IncomeRecord;
import hu.therealuhlarzoltan.expensables.microservices.cashflow.components.gateways.AccountGateway;
import hu.therealuhlarzoltan.expensables.microservices.cashflow.components.gateways.ExchangeGateway;
import hu.therealuhlarzoltan.expensables.microservices.cashflow.components.gateways.ExpenseGateway;
import hu.therealuhlarzoltan.expensables.microservices.cashflow.components.gateways.IncomeGateway;
import hu.therealuhlarzoltan.expensables.microservices.cashflow.components.sagas.ExpenseSaga;
import hu.therealuhlarzoltan.expensables.microservices.cashflow.components.sagas.IncomeSaga;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.ZonedDateTime;


@Service
@RequiredArgsConstructor
public class CashflowIntegrationImpl implements CashflowIntegration {
    private static final Logger LOG = LoggerFactory.getLogger(CashflowIntegrationImpl.class);
    private final AccountGateway accountGateway;
    private final ExpenseGateway expenseGateway;
    private final IncomeGateway incomeGateway;
    private final ExchangeGateway exchangeGateway;
    private final IncomeSaga incomeSaga;
    private final ExpenseSaga expenseSaga;

    @Override
    public Flux<IncomeRecord> getAccountIncomes(String accountId) {
        LOG.info("Will delegate the getAccountIncomes API call to the IncomeGateway with id: {}", accountId);
        return incomeGateway.getIncomes(accountId);
    }

    @Override
    public Flux<ExpenseRecord> getAccountExpenses(String accountId) {
        LOG.info("Will delegate the getAccountExpenses API call to the ExpensesGateway with id: {}", accountId);
        return expenseGateway.getExpenses(accountId);
    }

    @Override
    public Mono<IncomeRecord> getIncome(String incomeId) {
        LOG.info("Will delegate the getIncome API call to the IncomeGateway with id: {}", incomeId);
        return incomeGateway.getIncome(incomeId);
    }

    @Override
    public Mono<ExpenseRecord> getExpense(String expenseId) {
        LOG.info("Will delegate the getExpense API call to the ExpenseGateway with id: {}", expenseId);
        return expenseGateway.getExpense(expenseId);
    }

    @Override
    public Mono<Void> deleteIncome(IncomeRecord incomeRecord) {
        LOG.info("Will delegate the deleteIncome API call to the IncomeSaga with id: {}", incomeRecord.getRecordId());
        return incomeSaga.deleteIncome(incomeRecord);
    }

    @Override
    public Mono<Void> deleteExpense(ExpenseRecord expenseRecord) {
        LOG.info("Will delegate the deleteExpense API call to the ExpenseSaga with id: {}", expenseRecord.getRecordId());
        return expenseSaga.deleteExpense(expenseRecord);
    }

    @Override
    public Mono<Void> deleteExpenseWithExchange(ExpenseRecord expenseRecord, String targetCurrency) {
        LOG.info("Will delegate the deleteExpenseWithExchange API call to the ExpenseSaga with id: {}", expenseRecord.getRecordId());
        return exchangeGateway.makeExchange(expenseRecord.getCurrency(), targetCurrency, expenseRecord.getAmount(), expenseRecord.getExpenseDate())
                .flatMap(exchange -> expenseSaga.deleteExpense(expenseRecord, exchange.getResult()))
                .doOnError(throwable -> LOG.error("Error while deleting expense with exchange: {}", throwable.getMessage()));
    }

    @Override
    public Mono<String> getAccountCurrency(String accountId) {
        return accountGateway.getAccountCurrency(accountId);
    }

    @Override
    public Mono<IncomeRecord> createIncome(IncomeRecord incomeRecord) {
        return incomeSaga.createIncome(incomeRecord);
    }

    @Override
    public Mono<IncomeRecord> createIncomeWithExchange(IncomeRecord incomeRecord, String targetCurrency) {
        LOG.info("Will delegate the createIncomeWithExchange API call to the IncomeSaga with id: {}", incomeRecord.getRecordId());
        return exchangeGateway.makeExchange(incomeRecord.getCurrency(), targetCurrency, incomeRecord.getAmount(), ZonedDateTime.now())
                .flatMap(exchange -> incomeSaga.createIncome(incomeRecord, exchange.getResult()))
                .doOnError(throwable -> LOG.error("Error while creating income with exchange: {}", throwable.getMessage()));
    }

    @Override
    public Mono<Void> deleteIncomeWithExchange(IncomeRecord incomeRecord, String targetCurrency) {
        LOG.info("Will delegate the deleteIncomeWithExchange API call to the IncomeSaga with id: {}", incomeRecord.getRecordId());
        return exchangeGateway.makeExchange(incomeRecord.getCurrency(), targetCurrency, incomeRecord.getAmount(), incomeRecord.getIncomeDate())
                .flatMap(exchange -> incomeSaga.deleteIncome(incomeRecord, exchange.getResult()))
                .doOnError(throwable -> LOG.error("Error while deleting income with exchange: {}", throwable.getMessage()));
    }

    @Override
    public Mono<IncomeRecord> updateIncomeWithExchange(IncomeRecord incomeRecord, String targetCurrency) {
        LOG.info("Will delegate the updateIncomeWithExchange API call to the IncomeSaga with id: {}", incomeRecord.getRecordId());
        return exchangeGateway.makeExchange(incomeRecord.getCurrency(), targetCurrency, incomeRecord.getAmount(), incomeRecord.getIncomeDate())
                .flatMap(exchange -> incomeSaga.updateIncome(incomeRecord, exchange.getResult()))
                .doOnError(throwable -> LOG.error("Error while updating income with exchange: {}", throwable.getMessage()));
    }

    @Override
    public Mono<IncomeRecord> updateIncome(IncomeRecord incomeRecord) {
        LOG.info("Will delegate the updateIncome API call to the IncomeSaga with id: {}", incomeRecord.getRecordId());
        return incomeGateway.getIncome(incomeRecord.getRecordId())
                .flatMap(income -> incomeSaga.updateIncome(incomeRecord, incomeRecord.getAmount().subtract(income.getAmount())))
                .doOnError(throwable -> LOG.error("Error while updating income: {}", throwable.getMessage()));
    }

    @Override
    public Mono<ExpenseRecord> updateExpenseWithExchange(ExpenseRecord expenseRecord, String targetCurrency) {
        LOG.info("Will delegate the updateExpenseWithExchange API call to the ExpenseSaga with id: {}", expenseRecord.getRecordId());
        return exchangeGateway.makeExchange(expenseRecord.getCurrency(), targetCurrency, expenseRecord.getAmount(), expenseRecord.getExpenseDate())
                .flatMap(exchange -> expenseSaga.updateExpense(expenseRecord, exchange.getResult()))
                .doOnError(throwable -> LOG.error("Error while updating expense with exchange: {}", throwable.getMessage()));
    }

    @Override
    public Mono<ExpenseRecord> updateExpense(ExpenseRecord expenseRecord) {
        LOG.info("Will delegate the updateExpense API call to the ExpenseSaga with id: {}", expenseRecord.getRecordId());
        return expenseGateway.getExpense(expenseRecord.getRecordId())
                .flatMap(expense-> expenseSaga.updateExpense(expenseRecord, expenseRecord.getAmount().subtract(expense.getAmount())))
                .doOnError(throwable -> LOG.error("Error while updating income: {}", throwable.getMessage()));
    }

    @Override
    public Mono<ExpenseRecord> createExpense(ExpenseRecord expenseRecord) {
        LOG.info("Will delegate the createExpense API call to the ExpenseSaga with id: {}", expenseRecord.getRecordId());
        return expenseSaga.createExpense(expenseRecord).doOnError(throwable -> LOG.error("Error while creating expense: {}", throwable.getMessage()));
    }

    @Override
    public Mono<ExpenseRecord> createExpenseWithExchange(ExpenseRecord expenseRecord, String targetCurrency) {
        LOG.info("Will delegate the createExpenseWithExchange API call to the ExpenseSaga with id: {}", expenseRecord.getRecordId());
        return exchangeGateway.makeExchange(expenseRecord.getCurrency(), targetCurrency, expenseRecord.getAmount(), ZonedDateTime.now())
                .flatMap(exchange -> expenseSaga.createExpense(expenseRecord, exchange.getResult()))
                .doOnError(throwable -> LOG.error("Error while creating expenses with exchange: {}", throwable.getMessage()));
    }
}
