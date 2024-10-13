package hu.therealuhlarzoltan.expensables.microservices.income.services;

import com.mongodb.DuplicateKeyException;
import hu.therealuhlarzoltan.expensables.api.microservices.core.income.IncomeRecord;
import hu.therealuhlarzoltan.expensables.api.microservices.exceptions.InvalidInputDataException;
import hu.therealuhlarzoltan.expensables.api.microservices.exceptions.NotFoundException;
import hu.therealuhlarzoltan.expensables.microservices.income.components.mappers.IncomeRecordMapper;
import hu.therealuhlarzoltan.expensables.microservices.income.models.IncomeRecordEntity;
import hu.therealuhlarzoltan.expensables.microservices.income.repositories.IncomeRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Service
@Validated
@RequiredArgsConstructor
public class IncomeServiceImpl implements IncomeService {
    private static final Logger LOG = LoggerFactory.getLogger(IncomeServiceImpl.class);

    private final IncomeRecordMapper mapper;
    private final IncomeRepository repository;

    @Override
    public Flux<IncomeRecord> getAllIncomes() {
        LOG.info("Will retrieve all income records");
        return repository.findAll().map(mapper::entityToIncomeRecord);
    }

    @Override
    public Mono<IncomeRecord> getIncome(String recordId) {
        LOG.info("Will retrieve income record with ID: {}", recordId);
        return repository.findById(recordId)
                .switchIfEmpty(Mono.error(new NotFoundException("as")))
                .map(mapper::entityToIncomeRecord);
    }

    @Override
    public Flux<IncomeRecord> getIncomesByAccount(String accountId) {
        LOG.info("Will retrieve all income records for account with ID: {}", accountId);
        return repository.findAllByAccountId(accountId).map(mapper::entityToIncomeRecord);
    }

    @Override
    public Mono<IncomeRecord> createIncome(@Valid IncomeRecordEntity incomeRecord) {
        LOG.info("Will create income record with body: {}", incomeRecord);
        if (incomeRecord.getId() != null && incomeRecord.getId().length() != 24)
            throw new InvalidInputDataException("Id must be 24 characters long");
        incomeRecord.setVersion(null); //handled by mongo
        incomeRecord.setTimestamp(ZonedDateTime.now());
        return repository.save(incomeRecord)
                .onErrorMap(
                        DuplicateKeyException.class,
                        e -> new InvalidInputDataException("Record with ID: " + incomeRecord.getId() + " already exists"))
                .map(mapper::entityToIncomeRecord);
    }

    @Override
    public Mono<IncomeRecord> updateIncome(@Valid IncomeRecordEntity incomeRecord) {
        LOG.info("Will update income record with body: {}", incomeRecord);
        return repository.findById(incomeRecord.getId())
                .switchIfEmpty(Mono.error(new NotFoundException("Record with ID: " + incomeRecord.getId() + " not found")))
                .flatMap(existingRecord -> {
                    existingRecord.setAccountId(incomeRecord.getAccountId());
                    existingRecord.setName(incomeRecord.getName());
                    existingRecord.setCurrency(incomeRecord.getCurrency());
                    existingRecord.setAmount(incomeRecord.getAmount());
                    existingRecord.setCategory(incomeRecord.getCategory());
                    return repository.save(existingRecord);
                })
                .map(mapper::entityToIncomeRecord);
    }

    @Override
    public Mono<Void> deleteIncome(String recordId) {
        LOG.info("Will delete income record with ID: {}", recordId);
        return repository.deleteById(recordId);
    }

    @Override
    public Mono<Void> deleteIncomesByAccount(String accountId) {
        LOG.info("Will delete all income records for account ID: {}", accountId);
        return repository.deleteAllByAccountId(accountId);
    }
}
