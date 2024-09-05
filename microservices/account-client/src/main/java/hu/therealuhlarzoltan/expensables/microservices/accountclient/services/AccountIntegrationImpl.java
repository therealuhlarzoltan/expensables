package hu.therealuhlarzoltan.expensables.microservices.accountclient.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import hu.therealuhlarzoltan.expensables.api.microservices.composite.account.AccountInformation;
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
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URI;

import static java.util.logging.Level.FINE;

@Service
@RequiredArgsConstructor
public class AccountIntegrationImpl implements AccountIntegration {

    private static final Logger LOG = LoggerFactory.getLogger(AccountIntegrationImpl.class);

    @Value("${app.account-service-url}")
    private String ACCOUNT_SERVICE_URL;
    @Value("${app.expense-service-url}")
    private String EXPENSE_SERVICE_URL;
    @Value("${app.income-service-url}")
    private String INCOME_SERVICE_URL;
    @Value("${app.transaction-service-url}")
    private String TRANSACTION_SERVICE_URL;

    private final WebClient webClient;
    private final ObjectMapper mapper;

    @Override
    public Mono<AccountInformation> getAccountInformation(String accountId) {
        URI url = UriComponentsBuilder.fromUriString(ACCOUNT_SERVICE_URL + "/api/accounts/{transactionId}").build(accountId);
        LOG.debug("Will call the getAccount API from the integration layer on URL: {}", url);

        return webClient.get().uri(url)
                .retrieve().bodyToMono(AccountInformation.class).log(LOG.getName(), FINE)
                .onErrorMap(WebClientResponseException.class, ex -> handleException(ex));
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
