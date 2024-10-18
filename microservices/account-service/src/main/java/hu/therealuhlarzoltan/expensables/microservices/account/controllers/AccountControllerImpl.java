package hu.therealuhlarzoltan.expensables.microservices.account.controllers;

import hu.therealuhlarzoltan.expensables.api.microservices.core.account.Account;
import hu.therealuhlarzoltan.expensables.api.microservices.core.account.AccountController;

import hu.therealuhlarzoltan.expensables.microservices.account.services.AccountService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class AccountControllerImpl implements AccountController {

    private final static Logger LOG = LoggerFactory.getLogger(AccountControllerImpl.class);

    private final AccountService accountService;

    @Override
    public Flux<Account> getAccounts(@RequestParam(required = false) Long ownerId) {
        if (ownerId != null) {
            LOG.info("Received a GET request for accounts with ownerId: {}", ownerId);
            return accountService.getAccountsByOwnerId(ownerId);
        } else {
            LOG.info("Received a GET request for all accounts");
            return accountService.getAllAccounts();
        }
    }

    @Override
    public Mono<Account> getAccountById(UUID accountId) {
        LOG.info("Received a GET request for account with id: {}", accountId);
        return accountService.getAccountById(accountId);
    }

    @Override
    public Mono<Account> createAccount(Account account) {
        LOG.info("Received a POST request to create a new account with body: {}", account);
        return accountService.createAccount(account);
    }

    @Override
    public Mono<Account> updateAccount(Account account) {
        LOG.info("Received a PUT request to update account with body: {}", account);
        return accountService.updateAccount(account);
    }

    @Override
    public Mono<Void> deleteAccount(UUID accountId) {
        LOG.info("Received a DELETE request for account with id: {}", accountId);
        return accountService.deleteAccount(accountId);
    }

    @Override
    public Mono<Account> deposit(UUID accountId, BigDecimal amount) {
        LOG.info("Received a PUT request to deposit {} to account with id: {}", amount, accountId);
        return accountService.deposit(accountId, amount);
    }

    @Override
    public Mono<Account> withdraw(UUID accountId, BigDecimal amount) {
        LOG.info("Received a PUT request to withdraw {} from account with id: {}", amount, accountId);
        return accountService.withdraw(accountId, amount);
    }
}
