package hu.therealuhlarzoltan.expensables.api.microservices.composite.transaction;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface TransactionClientController {

    @GetMapping(value = "/api/transactions/account/in", produces = "application/json")
    Flux<TransactionInfo> getIncomingTransactions(@RequestParam UUID accountId);
    @GetMapping(value = "/api/transactions/account/out", produces = "application/json")
    Flux<TransactionInfo> getOutgoingTransactions(@RequestParam UUID accountId);
    @GetMapping(value = "/api/transactions/{transactionId}", produces = "application/json")
    Mono<TransactionInfo> getTransactionInfo(@PathVariable String transactionId);
    @PostMapping(value = "/api/transactions", produces = "application/json", consumes = "application/json")
    Mono<TransactionInfo> createTransaction(@RequestBody TransactionRequest transactionRequest);
    @PutMapping(value = "/api/transactions/{transactionId}", produces = "application/json", consumes = "application/json")
    Mono<TransactionInfo> updateTransaction(@PathVariable String transactionId, @RequestBody TransactionUpdateRequest updateRequest);
    @DeleteMapping(value = "/api/transactions/{transactionId}", produces = "application/json")
    @ResponseStatus(HttpStatus.ACCEPTED)
    Mono<Void> deleteTransaction(@PathVariable String transactionId);
}
