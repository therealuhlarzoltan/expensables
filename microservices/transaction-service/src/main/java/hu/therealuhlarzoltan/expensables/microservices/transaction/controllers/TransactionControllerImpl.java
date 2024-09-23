package hu.therealuhlarzoltan.expensables.microservices.transaction.controllers;

import hu.therealuhlarzoltan.expensables.api.microservices.core.transaction.TransactionController;
import hu.therealuhlarzoltan.expensables.api.microservices.core.transaction.TransactionRecord;
import hu.therealuhlarzoltan.expensables.microservices.transaction.components.TransactionMapper;
import hu.therealuhlarzoltan.expensables.microservices.transaction.services.TransactionService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class TransactionControllerImpl implements TransactionController {
    private static final Logger LOG = LoggerFactory.getLogger(TransactionControllerImpl.class);
    private final TransactionService service;
    private final TransactionMapper mapper;

    @Override
    public Flux<TransactionRecord> getTransactions(@RequestParam Optional<UUID> accountId, @RequestParam Optional<String> destination) {
        LOG.info("Received a GET requests for transactions with accountId: {} and destination: {}", accountId, destination);
        if ((accountId.isPresent() && destination.isEmpty()) || accountId.isEmpty() && destination.isPresent())
            return Flux.error(new IllegalArgumentException("Both accountId and destination must be provided"));
        if (accountId.isEmpty()) {
            return service.getAllTransactions();
        } else {
            if (!destination.get().equalsIgnoreCase("from") && !destination.get().equalsIgnoreCase("to"))
                return Flux.error(new IllegalArgumentException("Destination must be either 'from' or 'to'"));
            return service.getTransActionsForAccount(accountId.get(), !destination.get().equalsIgnoreCase("from"));
        }
    }

    @Override
    public Mono<TransactionRecord> getTransaction(@PathVariable String recordId) {
        LOG.info("Received a GET request for transaction with recordId: {}", recordId);
        return service.getTransaction(recordId);
    }

    @Override
    public Flux<TransactionRecord> getTransactionsByUser(@PathVariable Long userId) {
        LOG.info("Received a GET request for transactions with userId: {}", userId);
        return service.getTransactionsByUser(userId);
    }

    @Override
    public Mono<TransactionRecord> createTransaction(@RequestBody TransactionRecord transactionRecord) {
        LOG.info("Received a POST request to create a new transaction with body: {}", transactionRecord);
        var entity = mapper.apiToEntity(transactionRecord);
        return service.createTransaction(entity);
    }

    @Override
    public Mono<TransactionRecord> updateTransaction(@RequestBody TransactionRecord transactionRecord) {
        LOG.info("Received a PUT request to update a transaction with body: {}", transactionRecord);
        var entity = mapper.apiToEntity(transactionRecord);
        return service.updateTransaction(entity);
    }

    @Override
    public Mono<Void> deleteTransaction(@PathVariable String recordId) {
        LOG.info("Received a DELETE request to delete a transaction with recordId: {}", recordId);
        return service.deleteTransaction(recordId);
    }

    @Override
    public Mono<Void> deleteTransactionsByAccount(@RequestParam String accountId) {
        LOG.info("Received a DELETE request to delete all transactions with accountId: {}", accountId);
        return service.deleteTransactionsByAccount(accountId);
    }
}
