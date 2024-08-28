package hu.therealuhlarzoltan.expensables.microservices.income.services;

import hu.therealuhlarzoltan.expensables.microservices.income.models.IncomeRecordEntity;
import jakarta.validation.Valid;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IncomeService {
    Flux<IncomeRecord> getAllIncomes();
    Mono<IncomeRecord> getIncome(String recordId);
    Flux<IncomeRecord> getIncomesByAccount(String accountId);
    Mono<IncomeRecord> createIncome(@Valid IncomeRecordEntity incomeRecord);
    Mono<IncomeRecord> updateIncome(@Valid IncomeRecordEntity incomeRecord);
    Mono<Void> deleteIncome(String recordId);
}
