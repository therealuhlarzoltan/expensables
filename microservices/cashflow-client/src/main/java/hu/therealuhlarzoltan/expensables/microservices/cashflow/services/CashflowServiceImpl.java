package hu.therealuhlarzoltan.expensables.microservices.cashflow.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import hu.therealuhlarzoltan.expensables.api.microservices.composite.account.AccountInformation;
import hu.therealuhlarzoltan.expensables.api.microservices.composite.cashflow.ExpenseRecordInfo;
import hu.therealuhlarzoltan.expensables.api.microservices.composite.cashflow.IncomeRecordInfo;
import hu.therealuhlarzoltan.expensables.api.microservices.core.expense.ExpenseRecord;
import hu.therealuhlarzoltan.expensables.api.microservices.core.income.IncomeRecord;
import hu.therealuhlarzoltan.expensables.api.microservices.events.ResponsePayload;
import hu.therealuhlarzoltan.expensables.api.microservices.exceptions.InvalidInputDataException;
import hu.therealuhlarzoltan.expensables.api.microservices.exceptions.NotFoundException;
import hu.therealuhlarzoltan.expensables.api.microservices.exceptions.ServiceResponseException;
import hu.therealuhlarzoltan.expensables.microservices.cashflow.components.mappers.ExpenseMapper;
import hu.therealuhlarzoltan.expensables.microservices.cashflow.components.mappers.IncomeMapper;
import hu.therealuhlarzoltan.expensables.util.HttpErrorInfo;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

import static java.util.logging.Level.FINE;

@Service
@RequiredArgsConstructor
public class CashflowServiceImpl implements CashflowService {
    private static final Logger LOG = LoggerFactory.getLogger(CashflowServiceImpl.class);
    @Value("${app.income-service-url}")
    private String INCOME_SERVICE_URL;
    @Value("${app.expense-service-url}")
    private String EXPENSE_SERVICE_URL;
    private final IncomeMapper incomeMapper;
    private final ExpenseMapper expenseMapper;
    private final CashflowIntegration integration;
    private final WebClient webClient;
    private final ResponseListenerService responseListener;
    private final ObjectMapper mapper;


    @Override
    public Flux<IncomeRecordInfo> getAccountIncomes(String accountId) {
        LOG.info("Will call the getAccountIncomes API with accountId: {}", accountId);
        URI url = UriComponentsBuilder.fromUriString(INCOME_SERVICE_URL + "/api/incomes/{accountId}").build(accountId);
        return getForManyReactive(url, IncomeRecord.class, Duration.ofSeconds(5))
                .map(incomeMapper::incomeRecordToInfo);
    }

    @Override
    public Flux<ExpenseRecordInfo> getAccountExpenses(String accountId) {
        LOG.info("Will call the getAccountExpenses API with accountId: {}", accountId);
        URI url = UriComponentsBuilder.fromUriString(EXPENSE_SERVICE_URL + "/api/expenses/{accountId}").build(accountId);
        return getForManyReactive(url, ExpenseRecord.class, Duration.ofSeconds(5)).map(expenseMapper::expenseRecordToInfo);
    }

    @Override
    public Mono<IncomeRecordInfo> getIncome(String incomeId) {
        LOG.info("Will call the getIncome API with id: {}", incomeId);
        URI url = UriComponentsBuilder.fromUriString(INCOME_SERVICE_URL + "/api/incomes/get/{incomeId}").build(incomeId);
        return getForSingleReactive(url, IncomeRecord.class, Duration.ofSeconds(5))
                .map(incomeMapper::incomeRecordToInfo);
    }

    @Override
    public Mono<ExpenseRecordInfo> getExpense(String expenseId) {
        LOG.info("Will call the getExpense API with id: {}", expenseId);
        URI url = UriComponentsBuilder.fromUriString(EXPENSE_SERVICE_URL + "/api/expenses/get/{expenseId}").build(expenseId);
        return getForSingleReactive(url, ExpenseRecord.class, Duration.ofSeconds(5))
                .map(expenseMapper::expenseRecordToInfo);
    }

    @Override
    public Mono<IncomeRecordInfo> createIncome(IncomeRecordInfo incomeRecordInfo) {
        LOG.info("Will call the integration layer to create income: {}", incomeRecordInfo);
        return null;
    }

    @Override
    public Mono<ExpenseRecordInfo> createExpense(ExpenseRecordInfo expenseRecordInfo) {
        LOG.info("Will call the integration layer to create expense: {}", expenseRecordInfo);
        return null;
    }

    @Override
    public Mono<IncomeRecordInfo> updateIncome(String incomeId, IncomeRecordInfo incomeRecordInfo) {
        LOG.info("Will call the integration layer to update income with id: {} and body: {}", incomeId, incomeRecordInfo);
        return null;
    }

    @Override
    public Mono<ExpenseRecordInfo> updateExpense(String expenseId, ExpenseRecordInfo expenseRecordInfo) {
        LOG.info("Will call the integration layer to update expense with id: {} and body: {}", expenseId, expenseRecordInfo);
        return null;
    }

    @Override
    public Mono<Void> deleteIncome(String incomeId) {
        LOG.info("Will call the integration layer to delete income with id: {}", incomeId);
        return integration.deleteIncome(incomeId);
    }

    @Override
    public Mono<Void> deleteExpense(String expenseId) {
        LOG.info("Will call the integration layer to delete expense with id: {}", expenseId);
        return integration.deleteExpense(expenseId);
    }

    private <T> Mono<T> getForSingleReactive(URI url, Class<T> clazz, Duration timeout) {
        return webClient.get().uri(url)
                .retrieve().bodyToMono(clazz)
                .timeout(timeout)
                .log(LOG.getName(), FINE)
                .onErrorMap(Throwable.class, ex -> handleWebClientException(ex));
    }

    private <T> Flux<T> getForManyReactive(URI url, Class<T> clazz, Duration timeout) {
        return webClient.get().uri(url)
                .retrieve().bodyToFlux(clazz)
                .timeout(timeout)
                .log(LOG.getName(), FINE)
                .onErrorMap(Throwable.class, ex -> handleWebClientException(ex));
    }

    private <T> T deserializeObjectFromJson(String json, Class<T> clazz) {
        T obj = null;
        try {
            obj = mapper.readValue(json, clazz);
        } catch (IOException e) {
            LOG.error("Couldn't deserialize object from json: {}", e.getMessage());
        }
        return obj;
    }

    private Throwable createMessageResponseError(ResponsePayload data) {
        return new ServiceResponseException(data.getMessage(), data.getStatus());
    }

    private Throwable handleWebClientException(Throwable ex) {

        if (!(ex instanceof WebClientResponseException) && !(ex instanceof TimeoutException)) {
            LOG.warn("Got a unexpected error: {}, will rethrow it", ex.toString());
            return ex;
        }
        if (ex instanceof TimeoutException) {
            return new ServiceResponseException("Dependent service call failed", HttpStatus.FAILED_DEPENDENCY);
        }

        WebClientResponseException wcre = (WebClientResponseException)ex;

        if (wcre.getStatusCode().is5xxServerError()) {
            return new ServiceResponseException("Dependent service call failed", HttpStatus.FAILED_DEPENDENCY);
        }

        switch (HttpStatus.resolve(wcre.getStatusCode().value())) {
            case NOT_FOUND:
                return new NotFoundException(getErrorMessage(wcre));
            case BAD_REQUEST, UNPROCESSABLE_ENTITY:
                return new InvalidInputDataException(getErrorMessage(wcre));

            default:
                LOG.warn("Got an unexpected HTTP error: {}, will rethrow it", wcre.getStatusCode());
                LOG.warn("Error body: {}", wcre.getResponseBodyAsString());
                return ex;
        }
    }

    private String getErrorMessage(WebClientResponseException ex) {
        try {
            return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        } catch (IOException ioex) {
            return ex.getMessage();
        }
    }
}
