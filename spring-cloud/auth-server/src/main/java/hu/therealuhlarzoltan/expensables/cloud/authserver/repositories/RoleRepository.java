package hu.therealuhlarzoltan.expensables.cloud.authserver.repositories;

import hu.therealuhlarzoltan.expensables.cloud.authserver.models.RoleEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends CrudRepository<RoleEntity, Long> {
    Optional<RoleEntity> findByName(String name);
}
