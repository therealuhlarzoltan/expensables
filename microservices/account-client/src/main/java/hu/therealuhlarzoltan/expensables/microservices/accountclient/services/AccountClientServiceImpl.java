package hu.therealuhlarzoltan.expensables.microservices.accountclient.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import hu.therealuhlarzoltan.expensables.api.microservices.composite.account.AccountInformation;
import hu.therealuhlarzoltan.expensables.api.microservices.composite.account.AccountInformationAggregate;
import hu.therealuhlarzoltan.expensables.api.microservices.core.account.Account;
import hu.therealuhlarzoltan.expensables.api.microservices.events.HttpResponseEvent;
import hu.therealuhlarzoltan.expensables.api.microservices.events.ResponsePayload;
import hu.therealuhlarzoltan.expensables.api.microservices.exceptions.ServiceResponseException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountClientServiceImpl implements AccountClientService {
    private static final Logger LOG = LoggerFactory.getLogger(AccountClientServiceImpl.class);
    private final AccountIntegration integration;
    private final ResponseListenerService responseListener;
    private final ObjectMapper mapper;

    @Override
    public Mono<AccountInformation> getAccountInformation(String accountId) {
        LOG.info("Will call the integration layer for account information with accountId: {}", accountId);
        return integration.getAccountInformation(accountId);
    }

    @Override
    public Mono<AccountInformationAggregate> getAccountDetails(String accountId) {
        return null;
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
