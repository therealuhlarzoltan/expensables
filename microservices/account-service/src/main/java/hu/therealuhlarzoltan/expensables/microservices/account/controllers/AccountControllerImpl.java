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
            return accountService.getAccountsByOwnerId(ownerId);
        } else {
            return accountService.getAllAccounts();
        }
    }

    @Override
    public Mono<Account> getAccountById(UUID accountId) {
        return accountService.getAccountById(accountId);
    }

    @Override
    public Mono<Account> createAccount(Account account) {
        return accountService.createAccount(account);
    }

    @Override
    public Mono<Account> updateAccount(Account account) {
        return accountService.updateAccount(account);
    }

    @Override
    public Mono<Void> deleteAccount(UUID accountId) {
        return accountService.deleteAccount(accountId);
    }

    @Override
    public Mono<Account> deposit(UUID accountId, BigDecimal amount) {
        return accountService.deposit(accountId, amount);
    }

    @Override
    public Mono<Account> withdraw(UUID accountId, BigDecimal amount) {
        return accountService.withdraw(accountId, amount);
    }
}
