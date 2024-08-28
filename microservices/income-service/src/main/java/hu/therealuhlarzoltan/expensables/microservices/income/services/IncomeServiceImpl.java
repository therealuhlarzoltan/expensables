package hu.therealuhlarzoltan.expensables.microservices.income.services;

import hu.therealuhlarzoltan.expensables.microservices.income.components.mappers.IncomeRecordMapper;
import hu.therealuhlarzoltan.expensables.microservices.income.models.IncomeRecordEntity;
import hu.therealuhlarzoltan.expensables.microservices.income.repositories.IncomeRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class IncomeServiceImpl implements IncomeService {
    private static final Logger LOG = LoggerFactory.getLogger(IncomeServiceImpl.class);

    private final IncomeRecordMapper mapper;
    private final IncomeRepository repository;

    @Override
    public Flux<IncomeRecord> getAllIncomes() {
        LOG.info("Will retrieve all income records");
        return null;
    }

    @Override
    public Mono<IncomeRecord> getIncome(String recordId) {
        return null;
    }

    @Override
    public Flux<IncomeRecord> getIncomesByAccount(String accountId) {
        return null;
    }

    @Override
    public Mono<IncomeRecord> createIncome(IncomeRecordEntity incomeRecord) {
        return null;
    }

    @Override
    public Mono<IncomeRecord> updateIncome(IncomeRecordEntity incomeRecord) {
        return null;
    }

    @Override
    public Mono<Void> deleteIncome(String recordId) {
        return null;
    }
}
