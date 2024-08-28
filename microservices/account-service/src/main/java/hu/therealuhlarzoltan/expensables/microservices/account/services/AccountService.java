package hu.therealuhlarzoltan.expensables.microservices.account.services;

import hu.therealuhlarzoltan.expensables.api.microservices.core.account.Account;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

public interface AccountService {

    Flux<Account> getAllAccounts();
    Mono<Account> getAccountById(UUID accountId);
    Flux<Account> getAccountsByOwnerId(long ownerId);
    Mono<Account> createAccount(Account account);
    Mono<Account> updateAccount(Account account);
    Mono<Void> deleteAccount(UUID accountId);
    Mono<Account> deposit(UUID accountId, BigDecimal amount);
    Mono<Account> withdraw(UUID accountId, BigDecimal amount);
}
