package hu.therealuhlarzoltan.expensables.microservices.income.controllers;

import hu.therealuhlarzoltan.expensables.api.microservices.core.income.IncomeController;
import hu.therealuhlarzoltan.expensables.api.microservices.core.income.IncomeRecord;
import hu.therealuhlarzoltan.expensables.microservices.income.components.mappers.IncomeRecordMapper;
import hu.therealuhlarzoltan.expensables.microservices.income.services.IncomeService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class IncomeControllerImpl implements IncomeController {
    private static final Logger LOG = LoggerFactory.getLogger(IncomeControllerImpl.class);

    private final IncomeRecordMapper mapper;
    private final IncomeService service;

    @Override
    public Flux<IncomeRecord> getAllIncomes() {
        LOG.info("Received a GET request for all incomes");
        return service.getAllIncomes();
    }

    @Override
    public Mono<IncomeRecord> getIncome(String recordId) {
        LOG.info("Received a GET request for income with ID: {}", recordId);
        return service.getIncome(recordId);
    }

    @Override
    public Flux<IncomeRecord> getIncomesByAccount(String accountId) {
        LOG.info("Received a GET request for incomes by account with ID: {}", accountId);
        return service.getIncomesByAccount(accountId);
    }

    @Override
    public Mono<IncomeRecord> createIncome(IncomeRecord incomeRecord) {
        LOG.info("Received a POST request for income record with body: {}", incomeRecord);
        var entity = mapper.incomeRecordToEntity(incomeRecord);
        return service.createIncome(entity);
    }

    @Override
    public Mono<IncomeRecord> updateIncome(IncomeRecord incomeRecord) {
        LOG.info("Received a PUT request for income record with body: {}", incomeRecord);
        var entity = mapper.incomeRecordToEntity(incomeRecord);
        return service.updateIncome(entity);
    }

    @Override
    public Mono<Void> deleteIncome(String recordId) {
        LOG.info("Received a DELETE request for income with ID: {}", recordId);
        return service.deleteIncome(recordId);
    }
}
