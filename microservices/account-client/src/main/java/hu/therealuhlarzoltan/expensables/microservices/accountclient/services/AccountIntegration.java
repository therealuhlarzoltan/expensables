package hu.therealuhlarzoltan.expensables.microservices.accountclient.services;

import hu.therealuhlarzoltan.expensables.api.microservices.composite.account.AccountInformation;
import reactor.core.publisher.Mono;

public interface AccountIntegration {
    Mono<AccountInformation> getAccountInformation(String accountId);
}
