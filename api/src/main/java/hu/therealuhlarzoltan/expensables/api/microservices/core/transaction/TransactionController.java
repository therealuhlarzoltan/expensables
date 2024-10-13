package hu.therealuhlarzoltan.expensables.api.microservices.core.transaction;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;

public interface TransactionController {
    @GetMapping(value = "/api/transactions", produces = "application/json")
    Flux<TransactionRecord> getTransactions(@RequestParam Optional<UUID> accountId, @RequestParam Optional<String> destination);
    @GetMapping(value = "/api/transactions/{recordId}", produces = "application/json")
    Mono<TransactionRecord> getTransaction(@PathVariable String recordId);
    @GetMapping(value = "/api/transactions/user/{userId}", produces = "application/json")
    Flux<TransactionRecord> getTransactionsByUser(@PathVariable Long userId);
    @PostMapping(value = "/api/transactions", produces = "application/json", consumes = "application/json")
    Mono<TransactionRecord> createTransaction(@RequestBody TransactionRecord transactionRecord);
    @PutMapping(value = "/api/transactions", produces = "application/json", consumes = "application/json")
    Mono<TransactionRecord> updateTransaction(@RequestBody TransactionRecord transactionRecord);
    @DeleteMapping(value = "/api/transactions/{recordId}", produces = "application/json")
    Mono<Void> deleteTransaction(@PathVariable String recordId);
    @DeleteMapping(value = "/api/transactions", produces = "application/json")
    Mono<Void> deleteTransactionsByAccount(@RequestParam String accountId);
}
