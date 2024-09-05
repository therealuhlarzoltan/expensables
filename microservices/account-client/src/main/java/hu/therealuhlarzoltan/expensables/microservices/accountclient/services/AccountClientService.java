package hu.therealuhlarzoltan.expensables.microservices.accountclient.services;

import hu.therealuhlarzoltan.expensables.api.microservices.composite.account.AccountInformation;
import hu.therealuhlarzoltan.expensables.api.microservices.composite.account.AccountInformationAggregate;
import reactor.core.publisher.Mono;

public interface AccountClientService {
    Mono<AccountInformation> getAccountInformation(String accountId);

    Mono<AccountInformationAggregate> getAccountDetails(String accountId);

    Mono<AccountInformation> createAccount(AccountInformation account);

    Mono<AccountInformation> updateAccount(String accountId, AccountInformation account);

    Mono<Void> deleteAccount(String accountId);
}
