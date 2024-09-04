package hu.therealuhlarzoltan.expensables.api.microservices.composite.account;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

public interface AccountClientController {
    @GetMapping("/api/accounts/{accountId}")
    Mono<AccountInformation> getAccountInformation(@PathVariable String accountId);

    @GetMapping("/api/accounts/{accountId}/details")
    Mono<AccountInformationAggregate> getAccountDetails(@PathVariable String accountId);

    @PostMapping("/api/accounts/{accountId}")
    Mono<AccountInformation> createAccount(@PathVariable String accountId);

    @PutMapping("/api/accounts/{accountId}")
    Mono<AccountInformation> updateAccount(@PathVariable String accountId);

    @DeleteMapping("/api/accounts/{accountId}")
    Mono<Void> deleteAccount(@PathVariable String accountId);
}
