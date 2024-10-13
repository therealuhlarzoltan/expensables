package hu.therealuhlarzoltan.expensables.microservices.accountclient.controllers;

import hu.therealuhlarzoltan.expensables.api.microservices.composite.account.AccountClientController;
import hu.therealuhlarzoltan.expensables.api.microservices.composite.account.AccountInformation;
import hu.therealuhlarzoltan.expensables.api.microservices.composite.account.AccountInformationAggregate;
import hu.therealuhlarzoltan.expensables.microservices.accountclient.services.AccountClientService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class AccountClientControllerImpl implements AccountClientController {
    private static final Logger LOG = LoggerFactory.getLogger(AccountClientControllerImpl.class);
    private final AccountClientService service;

    @Override
    public Mono<AccountInformation> getAccountInformation(String accountId) {
        LOG.info("Received a GET request for account information with accountId: {}", accountId);
        return service.getAccountInformation(accountId);
    }

    @Override
    public Mono<AccountInformationAggregate> getAccountDetails(String accountId) {
        LOG.info("Received a GET request for account details with accountId: {}", accountId);
        return service.getAccountDetails(accountId);
    }

    @Override
    public Mono<AccountInformation> createAccount(AccountInformation account) {
        LOG.info("Received a POST request to create a new account with body: {}", account);
        return service.createAccount(account);
    }

    @Override
    public Mono<AccountInformation> updateAccount(String accountId, AccountInformation account) {
        LOG.info("Received a PUT request to update account with accountId: {} and body: {}", accountId, account);
        return service.updateAccount(accountId, account);
    }

    @Override
    public Mono<Void> deleteAccount(String accountId) {
        LOG.info("Received a DELETE request to delete account with accountId: {}", accountId);
        return service.deleteAccount(accountId);
    }
}
