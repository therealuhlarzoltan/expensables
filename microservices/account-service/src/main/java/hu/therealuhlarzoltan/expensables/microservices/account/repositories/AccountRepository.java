package hu.therealuhlarzoltan.expensables.microservices.account.repositories;

import hu.therealuhlarzoltan.expensables.microservices.account.models.AccountEntity;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface AccountRepository extends ReactiveMongoRepository<AccountEntity, String> {
    Mono<AccountEntity> findByEntityId(String entityId);
    Flux<AccountEntity> findByOwnerId(Long ownerId);
    Mono<Void> deleteByEntityId(String entityId);
}
