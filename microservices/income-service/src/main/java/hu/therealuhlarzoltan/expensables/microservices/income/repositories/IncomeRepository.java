package hu.therealuhlarzoltan.expensables.microservices.income.repositories;

import hu.therealuhlarzoltan.expensables.microservices.income.models.IncomeRecordEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface IncomeRepository extends ReactiveCrudRepository<IncomeRecordEntity, String> {
    Flux<IncomeRecordEntity> findAllByUserId(Long userId);
    Flux<IncomeRecordEntity> findAllByAccountId(String accountId);
    Mono<Void> deleteAllByAccountId(String accountId);
}
