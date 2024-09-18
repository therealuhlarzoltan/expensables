package hu.therealuhlarzoltan.expensables.microservices.accountclient.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import hu.therealuhlarzoltan.expensables.api.microservices.composite.account.AccountInformation;
import hu.therealuhlarzoltan.expensables.api.microservices.core.account.Account;
import hu.therealuhlarzoltan.expensables.api.microservices.core.expense.ExpenseRecord;
import hu.therealuhlarzoltan.expensables.api.microservices.core.income.IncomeRecord;
import hu.therealuhlarzoltan.expensables.api.microservices.core.transaction.TransactionRecord;
import hu.therealuhlarzoltan.expensables.api.microservices.events.CrudEvent;
import hu.therealuhlarzoltan.expensables.api.microservices.events.Event;
import hu.therealuhlarzoltan.expensables.api.microservices.exceptions.InvalidInputDataException;
import hu.therealuhlarzoltan.expensables.api.microservices.exceptions.NotFoundException;
import hu.therealuhlarzoltan.expensables.microservices.accountclient.components.gateways.AccountGateway;
import hu.therealuhlarzoltan.expensables.microservices.accountclient.components.gateways.ExpenseGateway;
import hu.therealuhlarzoltan.expensables.microservices.accountclient.components.gateways.IncomeGateway;
import hu.therealuhlarzoltan.expensables.microservices.accountclient.components.gateways.TransactionGateway;
import hu.therealuhlarzoltan.expensables.util.HttpErrorInfo;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;

import static java.util.logging.Level.FINE;

@Service
public class AccountIntegrationImpl implements AccountIntegration {

    private static final Logger LOG = LoggerFactory.getLogger(AccountIntegrationImpl.class);
    private final AccountGateway accountGateway;
    private final IncomeGateway incomeGateway;
    private final ExpenseGateway expenseGateway;
    private final TransactionGateway transactionGateway;
    private final Scheduler publishEventScheduler;
    private final StreamBridge streamBridge;
    private final WebClient webClient;
    private final ObjectMapper mapper;

    @Autowired
    public AccountIntegrationImpl(
            @Qualifier("publishEventScheduler") Scheduler publishEventScheduler,
            WebClient webClient,
            ObjectMapper mapper,
            StreamBridge streamBridge,
            AccountGateway accountGateway,
            IncomeGateway incomeGateway,
            ExpenseGateway expenseGateway,
            TransactionGateway transactionGateway) {
        this.publishEventScheduler = publishEventScheduler;
        this.webClient = webClient;
        this.mapper = mapper;
        this.streamBridge = streamBridge;
        this.accountGateway = accountGateway;
        this.incomeGateway = incomeGateway;
        this.expenseGateway = expenseGateway;
        this.transactionGateway = transactionGateway;
    }

    @Override
    public Mono<Account> getAccount(String accountId) {
        LOG.debug("Will delegate the getAccount API call to the AccountGateway");
        return accountGateway.getAccount(accountId);
    }

    @Override
    public Flux<IncomeRecord> getIncomes(String accountId) {
        LOG.debug("Will delegate the getIncomes API call to the IncomeGateway");
        return incomeGateway.getIncomes(accountId);
    }

    @Override
    public Flux<IncomeRecord> getIncomesWithFallback(String accountId) {
        LOG.debug("Will delegate the getIncomesWithFallback API call to the IncomeGateway");
        return incomeGateway.getIncomesWithFallback(accountId);
    }

    @Override
    public Flux<ExpenseRecord> getExpenses(String accountId) {
        LOG.debug("Will delegate the getExpenses API call to the ExpenseGateway");
        return expenseGateway.getExpenses(accountId);
    }

    @Override
    public Flux<ExpenseRecord> getExpensesWithFallback(String accountId) {
        LOG.debug("Will delegate the getExpensesWithFallback API call to the ExpenseGateway");
        return expenseGateway.getExpensesWithFallback(accountId);
    }

    @Override
    public Flux<TransactionRecord> getIncomingTransactions(String accountId) {
        LOG.debug("Will delegate the getIncomingTransactions API call to the TransactionGateway");
        return transactionGateway.getIncomingTransactions(accountId);
    }

    @Override
    public Flux<TransactionRecord> getIncomingTransactionsWithFallback(String accountId) {
        LOG.debug("Will delegate the getIncomingTransactionsWithFallback API call to the TransactionGateway");
        return transactionGateway.getIncomingTransactionsWithFallback(accountId);
    }

    @Override
    public Flux<TransactionRecord> getOutgoingTransactions(String accountId) {
        LOG.debug("Will delegate the getOutgoingTransactions API call to the TransactionGateway");
        return transactionGateway.getOutgoingTransactions(accountId);
    }

    @Override
    public Flux<TransactionRecord> getOutgoingTransactionsWithFallback(String accountId) {
        LOG.debug("Will delegate the getOutgoingTransactionsWithFallback API call to the TransactionGateway");
        return transactionGateway.getOutgoingTransactionsWithFallback(accountId);
    }

    @Override
    public Mono<Account> getAccountWithFallback(String fromAccountId) {
        LOG.debug("Will delegate the getAccountWithFallback API call to the AccountGateway");
        return accountGateway.getAccountWithFallback(fromAccountId);
    }

    @Override
    public Mono<AccountInformation> createAccount(Account account, Optional<String> correlationId) {
        LOG.info("Will call the createAccount API from the integration layer with body: {}", account);
        String corrId = correlationId.orElse(null);
        return Mono.fromCallable(() -> {
            sendMessage("accounts-out-0", corrId, new CrudEvent<String, Account>(CrudEvent.Type.CREATE, account.getAccountId(), account));
            account.setVersion(0);
            return AccountInformation.builder()
                    .accountId(account.getAccountId())
                    .accountName(account.getAccountName())
                    .accountType(account.getAccountType())
                    .accountCategory(account.getAccountCategory())
                    .balance(account.getBalance())
                    .bankName(account.getBankName())
                    .currency(account.getCurrency())
                    .ownerId(account.getOwnerId())
                    .version(0)
                    .build();
        }).subscribeOn(publishEventScheduler);
    }

    @Override
    public Mono<AccountInformation> updateAccount(Account account, Optional<String> correlationId) {
        LOG.info("Will call the updateAccount API from the integration layer with body: {}", account);
        String corrId = correlationId.orElse(null);
        return Mono.fromCallable(() -> {
            sendMessage("accounts-out-0", corrId, new CrudEvent<String, Account>(CrudEvent.Type.UPDATE, account.getAccountId(), account));
            return AccountInformation.builder()
                    .accountId(account.getAccountId())
                    .accountName(account.getAccountName())
                    .accountType(account.getAccountType())
                    .accountCategory(account.getAccountCategory())
                    .balance(account.getBalance())
                    .bankName(account.getBankName())
                    .currency(account.getCurrency())
                    .ownerId(account.getOwnerId())
                    .version(account.getVersion() + 1)
                    .build();
        }).subscribeOn(publishEventScheduler);
    }

    @Override
    public Mono<Void> deleteAccount(Account account) {
        LOG.info("Will call the deleteAccount API from the integration layer with accountId: {}", account.getAccountId());
        return Mono.fromRunnable(() -> sendMessage("accounts-out-0", new CrudEvent<String, Account>(CrudEvent.Type.DELETE, account.getAccountId(), account)));
    }

    private void sendMessage(String bindingName, String correlationId, Event<?, ?> event) {
        LOG.debug("Sending a {} message to {}", event.getEventType(), bindingName);
        Message message = MessageBuilder.withPayload(event)
                .setHeader("partitionKey", event.getKey())
                .setHeader("correlationId", correlationId)
                .build();
        streamBridge.send(bindingName, message);
    }

    private void sendMessage(String bindingName, Event<?, ?> event) {
        LOG.debug("Sending a {} message to {}", event.getEventType(), bindingName);
        Message message = MessageBuilder.withPayload(event)
                .setHeader("partitionKey", event.getKey())
                .build();
        streamBridge.send(bindingName, message);
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
