package hu.therealuhlarzoltan.expensables.api.microservices.composite.account;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

public interface AccountClientController {
    @GetMapping(value = "/api/accounts/{accountId}", produces = "application/json")
    Mono<AccountInformation> getAccountInformation(@PathVariable String accountId);

    @GetMapping(value = "/api/accounts/details/{accountId}", produces = "application/json")
    Mono<AccountInformationAggregate> getAccountDetails(@PathVariable String accountId);

    @PostMapping(value = "/api/accounts", produces = "application/json", consumes = "application/json")
    Mono<AccountInformation> createAccount(@RequestBody AccountInformation accountInformation);

    @PutMapping(value = "/api/accounts/{accountId}", produces = "application/json", consumes = "application/json")
    Mono<AccountInformation> updateAccount(@PathVariable String accountId, @RequestBody AccountInformation accountInformation);

    @DeleteMapping(value = "/api/accounts/{accountId}", produces = "application/json")
    @ResponseStatus(HttpStatus.ACCEPTED)
    Mono<Void> deleteAccount(@PathVariable String accountId);
}
