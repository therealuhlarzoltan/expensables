package hu.therealuhlarzoltan.expensables.microservices.accountclient.components.gateways;

import hu.therealuhlarzoltan.expensables.api.microservices.core.account.Account;
import reactor.core.publisher.Mono;

public interface AccountGateway {
    Mono<Account> getAccount(String accountId);
    Mono<Account> getAccountWithFallback(String accountId);
}
