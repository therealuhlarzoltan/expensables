package hu.therealuhlarzoltan.expensables.microservices.accountclient.services;

import hu.therealuhlarzoltan.expensables.api.microservices.composite.account.AccountInformation;
import hu.therealuhlarzoltan.expensables.api.microservices.core.account.Account;
import reactor.core.publisher.Mono;

public interface AccountIntegration {
    Mono<AccountInformation> getAccountInformation(String accountId);
    Mono<AccountInformation> createAccount(Account account);
}
