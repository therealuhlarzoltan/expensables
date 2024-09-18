package hu.therealuhlarzoltan.expensables.microservices.transaction.services;

import com.mongodb.DuplicateKeyException;
import hu.therealuhlarzoltan.expensables.api.microservices.core.transaction.TransactionRecord;
import hu.therealuhlarzoltan.expensables.api.microservices.exceptions.InvalidInputDataException;
import hu.therealuhlarzoltan.expensables.api.microservices.exceptions.NotFoundException;
import hu.therealuhlarzoltan.expensables.microservices.transaction.components.TransactionMapper;
import hu.therealuhlarzoltan.expensables.microservices.transaction.models.TransactionEntity;
import hu.therealuhlarzoltan.expensables.microservices.transaction.repositories.TransactionRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Validated
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    private static final Logger LOG = LoggerFactory.getLogger(TransactionServiceImpl.class);
    private final TransactionMapper mapper;
    private final TransactionRepository repository;

    @Override
    public Flux<TransactionRecord> getAllTransactions() {
        LOG.info("Retrieving all transactions");
        return repository.findAll().map(mapper::entityToApi);
    }

    @Override
    public Flux<TransactionRecord> getTransActionsForAccount(UUID accountId, boolean toAccount) {
        LOG.info("Retrieving transactions for account with accountId: {} and destination: {}", accountId, toAccount ? "to" : "from");
        if (toAccount) {
            return repository.findAllByToAccountId(accountId.toString()).map(mapper::entityToApi);
        } else {
            return repository.findAllByFromAccountId(accountId.toString()).map(mapper::entityToApi);
        }
    }

    @Override
    public Mono<TransactionRecord> getTransaction(String recordId) {
        LOG.info("Retrieving transaction with recordId: {}", recordId);
        return repository.findById(recordId)
                .switchIfEmpty(Mono.error(new NotFoundException("Transaction with recordId: " + recordId + " not found")))
                .map(mapper::entityToApi);
    }

    @Override
    public Flux<TransactionRecord> getTransactionsByUser(Long userId) {
        LOG.info("Retrieving transactions for user with userId: {}", userId);
        return repository.findAllByUserId(userId)
                .map(mapper::entityToApi);
    }

    @Override
    public Mono<TransactionRecord> createTransaction(@Valid TransactionEntity entity) {
        LOG.info("Creating new transaction with body: {}", entity);
        if (entity.getId() != null && entity.getId().length() != 24)
            throw  new InvalidInputDataException("Transaction id must be 24 characters long");
        if (entity.getFromAccountId().equals(entity.getToAccountId()))
            throw new InvalidInputDataException("From and to account ids must be different");
        entity.setVersion(null); //handled by MongoDB
        entity.setTimestamp(LocalDateTime.now());
        return repository.save(entity)
                .onErrorMap(
                        DuplicateKeyException.class,
                        e -> new InvalidInputDataException("Transaction with id: " + entity.getId() + " already exists")
                )
                .map(mapper::entityToApi);
    }

    @Override
    public Mono<TransactionRecord> updateTransaction(@Valid TransactionEntity entity) {
        LOG.info("Updating transaction with body: {}", entity);
        if (entity.getFromAccountId().equals(entity.getToAccountId()))
            throw new InvalidInputDataException("From and to account ids must be different");
        return repository.findById(entity.getId())
                .switchIfEmpty(Mono.error(new NotFoundException("Transaction with recordId: " + entity.getId() + " not found")))
                .flatMap(existingEntity -> {
                    existingEntity.setAmount(entity.getAmount());
                    existingEntity.setFromCurrency(entity.getFromCurrency());
                    existingEntity.setToCurrency(entity.getToCurrency());
                    existingEntity.setFromAccountId(entity.getFromAccountId());
                    existingEntity.setToAccountId(entity.getToAccountId());
                    return repository.save(entity);
                })
                .map(mapper::entityToApi);
    }

    @Override
    public Mono<Void> deleteTransaction(String recordId) {
        LOG.info("Deleting transaction with recordId: {}", recordId);
        return repository.deleteById(recordId);
    }
}
