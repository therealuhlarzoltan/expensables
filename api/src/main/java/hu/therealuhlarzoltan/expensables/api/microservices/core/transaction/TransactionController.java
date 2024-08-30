package hu.therealuhlarzoltan.expensables.api.microservices.core.transaction;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;

public interface TransactionController {
    @GetMapping(value = "/api/transactions")
    Flux<TransactionRecord> getTransactions(@RequestParam Optional<UUID> accountId, @RequestParam Optional<String> destination);
    @GetMapping(value = "/api/transactions/{recordId}")
    Mono<TransactionRecord> getTransaction(@PathVariable String recordId);
    @GetMapping(value = "/api/transactions/user/{userId}")
    Flux<TransactionRecord> getTransactionsByUser(@PathVariable Long userId);
    @PostMapping(value = "/api/transactions")
    Mono<TransactionRecord> createTransaction(@RequestBody TransactionRecord transactionRecord);
    @PutMapping(value = "/api/transactions")
    Mono<TransactionRecord> updateTransaction(@RequestBody TransactionRecord transactionRecord);
    @DeleteMapping(value = "/api/transactions/{recordId}")
    Mono<Void> deleteTransaction(@PathVariable String recordId);
}
