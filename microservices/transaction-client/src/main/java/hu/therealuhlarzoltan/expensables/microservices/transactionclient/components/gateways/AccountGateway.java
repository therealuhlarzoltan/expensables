package hu.therealuhlarzoltan.expensables.microservices.transactionclient.components.gateways;

import hu.therealuhlarzoltan.expensables.api.microservices.core.account.Account;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.net.URI;

public interface AccountGateway {
    Mono<Account> getAccount(String accountId);
    Mono<Account> getAccountWithFallback(String accountId);
    Mono<String> getAccountCurrency(String accountId);
    Mono<BigDecimal> getAccountBalance(String accountId);
}
