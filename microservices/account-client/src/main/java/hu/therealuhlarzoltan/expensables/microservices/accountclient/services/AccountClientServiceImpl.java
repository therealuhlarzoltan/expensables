package hu.therealuhlarzoltan.expensables.microservices.accountclient.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import hu.therealuhlarzoltan.expensables.api.microservices.composite.account.*;
import hu.therealuhlarzoltan.expensables.api.microservices.core.account.Account;
import hu.therealuhlarzoltan.expensables.api.microservices.core.transaction.TransactionRecord;
import hu.therealuhlarzoltan.expensables.api.microservices.events.HttpResponseEvent;
import hu.therealuhlarzoltan.expensables.api.microservices.events.ResponsePayload;
import hu.therealuhlarzoltan.expensables.api.microservices.exceptions.ServiceResponseException;
import hu.therealuhlarzoltan.expensables.microservices.accountclient.components.mappers.AccountInformationMapper;
import hu.therealuhlarzoltan.expensables.microservices.accountclient.components.mappers.ExpenseMapper;
import hu.therealuhlarzoltan.expensables.microservices.accountclient.components.mappers.IncomeMapper;
import hu.therealuhlarzoltan.expensables.microservices.accountclient.components.mappers.TransactionMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountClientServiceImpl implements AccountClientService {
    private static final Logger LOG = LoggerFactory.getLogger(AccountClientServiceImpl.class);
    private final AccountIntegration integration;
    private final ResponseListenerService responseListener;
    private final ObjectMapper mapper;
    private final AccountInformationMapper accountInformationMapper;
    private final ExpenseMapper expenseMapper;
    private final IncomeMapper incomeMapper;
    private final TransactionMapper transactionMapper;

    @Override
    public Mono<AccountInformation> getAccountInformation(String accountId) {
        LOG.info("Will call the integration layer for account information with accountId: {}", accountId);
        return integration.getAccount(accountId).map(accountInformationMapper::entityToApi);
    }

    @Override
    public Mono<AccountInformationAggregate> getAccountDetails(String accountId) {
        LOG.info("Will call the integration layer for account information with accountId: {}", accountId);

        return integration.getAccount(accountId)
                .flatMap(account -> {
                    LOG.info("Resolving income records for account with accountId: {}", accountId);
                    Mono<List<IncomeInfo>> incomeInfoMono = integration.getIncomesWithFallback(accountId)
                            .map(incomeMapper::entityToApi)
                            .collectList();
                    LOG.info("Resolving expense records for account with accountId: {}", accountId);
                    Mono<List<ExpenseInfo>> expenseInfoMono = integration.getExpensesWithFallback(accountId)
                            .map(expenseMapper::entityToApi)
                            .collectList();

                    LOG.info("Resolving incoming transactions for account with accountId: {}", accountId);
                    Mono<List<TransactionIncomeInfo>> incomingTransactionMono = integration.getIncomingTransactionsWithFallback(accountId)
                            .map(transactionMapper::incomeTransactionToApi)
                            .flatMap(transaction ->
                                    integration.getAccountWithFallback(transaction.getFromAccountId())
                                            .map(a -> {
                                                transaction.setFromAccountName(a.getAccountName());
                                                return transaction;
                                            }))
                            .collectList();

                    LOG.info("Resolving outgoing transactions for account with accountId: {}", accountId);
                    Mono<List<TransactionExpenseInfo>> outgoingTransactionMono = integration.getOutgoingTransactionsWithFallback(accountId)
                            .map(transactionMapper::expenseTransactionToApi)
                            .flatMap(transaction ->
                                    integration.getAccountWithFallback(transaction.getToAccountId())
                                            .map(a -> {
                                                transaction.setToAccountName(a.getAccountName());
                                                return transaction;
                                            }))
                            .collectList();

                    return Mono.zip(incomeInfoMono, expenseInfoMono, incomingTransactionMono, outgoingTransactionMono)
                            .map(tuple -> {
                                List<IncomeInfo> incomes = tuple.getT1();
                                List<ExpenseInfo> expenses = tuple.getT2();
                                List<TransactionIncomeInfo> incomingTransactions = tuple.getT3();
                                List<TransactionExpenseInfo> outgoingTransactions = tuple.getT4();
                                LOG.info("Aggregating information for account with accountId: {}", accountId);
                                AccountInformationAggregate accountInformationAggregate =
                                        new AccountInformationAggregate(accountInformationMapper.entityToApi(account));
                                accountInformationAggregate.getIncomes().addAll(incomes);
                                accountInformationAggregate.getIncomes().addAll(incomingTransactions);
                                accountInformationAggregate.getExpenses().addAll(expenses);
                                accountInformationAggregate.getExpenses().addAll(outgoingTransactions);

                                return accountInformationAggregate;
                            });
                });
    }

    @Override
    public Mono<AccountInformation> createAccount(AccountInformation account) {
        String correlationId = UUID.randomUUID().toString();
        Account accountApi = Account.builder()
                .version(null)
                .accountId(UUID.randomUUID().toString())
                .ownerId(account.getOwnerId())
                .accountName(account.getAccountName())
                .accountType(account.getAccountType())
                .accountCategory(account.getAccountCategory())
                .balance(account.getBalance())
                .bankName(account.getBankName())
                .currency(account.getCurrency())
                .build();
        LOG.info("Will call the integration layer to create a new account with body: {}", accountApi);
        return integration.createAccount(accountApi, Optional.of(correlationId))
                .then(responseListener.waitForResponse(correlationId, Duration.ofSeconds(5)))
                .flatMap(response -> {
                    if (response.getEventType() == HttpResponseEvent.Type.SUCCESS) {
                        String jsonString = response.getData().getMessage();
                        AccountInformation createdAccount = deserializeObjectFromJson(jsonString, AccountInformation.class);
                        return Mono.just(createdAccount);
                    } else if (response.getEventType() == HttpResponseEvent.Type.ERROR) {
                        Throwable error = createMessageResponseError(response.getData());
                        return Mono.error(error);
                    } else {
                        Throwable ex = new ServiceResponseException("Couldn't complete request", HttpStatus.FAILED_DEPENDENCY);
                        return Mono.error(ex);
                    }
                });
    }

    @Override
    public Mono<AccountInformation> updateAccount(String accountId, AccountInformation account) {
        LOG.info("Will call the integration layer to update account with accountId: {}", accountId);
        String correlationId = UUID.randomUUID().toString();
        Account accountApi = Account.builder()
                .accountId(accountId)
                .ownerId(account.getOwnerId())
                .accountName(account.getAccountName())
                .accountType(account.getAccountType())
                .accountCategory(account.getAccountCategory())
                .balance(account.getBalance())
                .bankName(account.getBankName())
                .currency(account.getCurrency())
                .version(account.getVersion())
                .build();
        return integration.updateAccount(accountApi, Optional.of(correlationId))
                .then(responseListener.waitForResponse(correlationId, Duration.ofSeconds(5)))
                .flatMap(response -> {
                  if (response.getEventType() == HttpResponseEvent.Type.SUCCESS) {
                      String jsonString = response.getData().getMessage();
                        AccountInformation updatedAccount = deserializeObjectFromJson(jsonString, AccountInformation.class);
                      return Mono.just(updatedAccount);
                  } else if (response.getEventType() == HttpResponseEvent.Type.ERROR) {
                      Throwable error = createMessageResponseError(response.getData());
                      return Mono.error(error);
                  } else {
                      Throwable ex = new ServiceResponseException("Couldn't complete request", HttpStatus.FAILED_DEPENDENCY);
                      return Mono.error(ex);
                  }
                });
    }

    @Override
    public Mono<Void> deleteAccount(String accountId) {
        LOG.info("Will call the integration layer to delete account with accountId: {}", accountId);
        Account account = Account.builder().accountId(accountId).build();
        return integration.deleteAccount(account);
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
}
