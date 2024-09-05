package hu.therealuhlarzoltan.expensables.api.microservices.composite.account;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

public interface AccountClientController {
    @GetMapping(value = "/api/accounts/{accountId}", produces = "application/json")
    Mono<AccountInformation> getAccountInformation(@PathVariable String accountId);

    @GetMapping(value = "/api/accounts/{accountId}/details", produces = "application/json")
    Mono<AccountInformationAggregate> getAccountDetails(@PathVariable String accountId);

    @PostMapping(value = "/api/accounts", produces = "application/json", consumes = "application/json")
    Mono<AccountInformation> createAccount(@RequestBody AccountInformation accountInformation);

    @PutMapping(value = "/api/accounts/{accountId}", produces = "application/json", consumes = "application/json")
    Mono<AccountInformation> updateAccount(@PathVariable String accountId, @RequestBody AccountInformation accountInformation);

    @DeleteMapping(value = "/api/accounts/{accountId}", produces = "application/json")
    Mono<Void> deleteAccount(@PathVariable String accountId);
}
