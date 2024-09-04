package hu.therealuhlarzoltan.expensables.api.microservices.composite.transaction;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

public interface TransactionClientController {
    @GetMapping(value = "/api/transactions/{transactionId}")
    Mono<TransactionInfo> getTransactionInfo(@PathVariable String transactionId);
    @PostMapping(value = "/api/transactions")
    Mono<TransactionInfo> createTransaction(@RequestBody TransactionRequest transactionRequest);
    @PutMapping(value = "/api/transactions/{transactionId}")
    Mono<TransactionInfo> updateTransaction(@PathVariable String transactionId, @RequestBody TransactionRequest transactionRequest);
    @DeleteMapping(value = "/api/transactions/{transactionId}")
    Mono<Void> deleteTransaction(@PathVariable String transactionId);
}
