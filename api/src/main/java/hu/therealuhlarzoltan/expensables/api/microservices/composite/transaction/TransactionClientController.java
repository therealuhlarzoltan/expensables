package hu.therealuhlarzoltan.expensables.api.microservices.composite.transaction;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

public interface TransactionClientController {
    @GetMapping(value = "/api/transactions/{transactionId}", produces = "application/json")
    Mono<TransactionInfo> getTransactionInfo(@PathVariable String transactionId);
    @PostMapping(value = "/api/transactions", produces = "application/json", consumes = "application/json")
    Mono<TransactionInfo> createTransaction(@RequestBody TransactionRequest transactionRequest);
    @PutMapping(value = "/api/transactions/{transactionId}", produces = "application/json", consumes = "application/json")
    Mono<TransactionInfo> updateTransaction(@PathVariable String transactionId, @RequestBody TransactionRequest transactionRequest);
    @DeleteMapping(value = "/api/transactions/{transactionId}", produces = "application/json")
    Mono<Void> deleteTransaction(@PathVariable String transactionId);
}
