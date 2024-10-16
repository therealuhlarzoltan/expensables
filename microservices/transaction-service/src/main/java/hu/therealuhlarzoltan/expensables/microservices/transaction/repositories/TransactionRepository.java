package hu.therealuhlarzoltan.expensables.microservices.transaction.repositories;

import hu.therealuhlarzoltan.expensables.microservices.transaction.models.TransactionEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface TransactionRepository extends ReactiveCrudRepository<TransactionEntity, String> {
    Flux<TransactionEntity> findAllByFromAccountId(String accountId);
    Flux<TransactionEntity> findAllByToAccountId(String accountId);
    Flux<TransactionEntity> findAllByUserId(Long userId);
    Mono<Void> deleteAllByFromAccountIdOrToAccountId(String fromAccountId, String toAccountId);
}
