package hu.therealuhlarzoltan.expensables.microservices.expense.repositories;

import hu.therealuhlarzoltan.expensables.microservices.expense.models.ExpenseRecordEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ExpenseRepository extends ReactiveCrudRepository<ExpenseRecordEntity, String> {
    Flux<ExpenseRecordEntity> findAllByUserId(Long userId);
    Flux<ExpenseRecordEntity> findAllByAccountId(String accountId);
    Mono<Void> deleteAllByAccountId(String accountId);
}
