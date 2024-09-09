package hu.therealuhlarzoltan.expensables.microservices.cashflow.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import hu.therealuhlarzoltan.expensables.api.microservices.core.expense.ExpenseRecord;
import hu.therealuhlarzoltan.expensables.api.microservices.core.income.IncomeRecord;
import hu.therealuhlarzoltan.expensables.api.microservices.exceptions.InvalidInputDataException;
import hu.therealuhlarzoltan.expensables.api.microservices.exceptions.NotFoundException;
import hu.therealuhlarzoltan.expensables.util.HttpErrorInfo;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class CashflowIntegrationImpl implements CashflowIntegration {
    private static final Logger LOG = LoggerFactory.getLogger(CashflowIntegrationImpl.class);

    @Value("${app.account-service-url}")
    private String ACCOUNT_SERVICE_URL;
    @Value("${app.expense-service-url}")
    private String EXPENSE_SERVICE_URL;
    @Value("${app.income-service-url}")
    private String INCOME_SERVICE_URL;
    @Value("${app.exchange-service-url}")
    private String EXCHANGE_SERVICE_URL;

    private final WebClient webClient;
    private final ObjectMapper mapper;

    @Override
    public Mono<Void> deleteIncome(String incomeId) {
        return null;
    }

    @Override
    public Mono<Void> deleteExpense(String expenseId) {
        return null;
    }

    private Throwable handleException(Throwable ex) {

        if (!(ex instanceof WebClientResponseException)) {
            LOG.warn("Got a unexpected error: {}, will rethrow it", ex.toString());
            return ex;
        }

        WebClientResponseException wcre = (WebClientResponseException)ex;

        switch (HttpStatus.resolve(wcre.getStatusCode().value())) {
            case NOT_FOUND:
                return new NotFoundException(getErrorMessage(wcre));
            case UNPROCESSABLE_ENTITY:
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
