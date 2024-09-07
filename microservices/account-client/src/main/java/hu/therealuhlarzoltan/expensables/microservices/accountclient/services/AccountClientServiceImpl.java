package hu.therealuhlarzoltan.expensables.microservices.accountclient.services;

import hu.therealuhlarzoltan.expensables.api.microservices.composite.account.AccountInformation;
import hu.therealuhlarzoltan.expensables.api.microservices.composite.account.AccountInformationAggregate;
import hu.therealuhlarzoltan.expensables.api.microservices.core.account.Account;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountClientServiceImpl implements AccountClientService {
    private static final Logger LOG = LoggerFactory.getLogger(AccountClientServiceImpl.class);
    private final AccountIntegration integration;

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
        return integration.createAccount(accountApi);
    }

    @Override
    public Mono<AccountInformation> updateAccount(String accountId, AccountInformation account) {
        return null;
    }

    @Override
    public Mono<Void> deleteAccount(String accountId) {
        return null;
    }
}
