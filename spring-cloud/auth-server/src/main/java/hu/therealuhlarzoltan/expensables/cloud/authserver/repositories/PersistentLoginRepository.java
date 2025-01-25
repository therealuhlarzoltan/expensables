package hu.therealuhlarzoltan.expensables.cloud.authserver.repositories;

import hu.therealuhlarzoltan.expensables.cloud.authserver.models.PersistentLoginEntity;
import org.springframework.data.repository.CrudRepository;

public interface PersistentLoginRepository extends CrudRepository<PersistentLoginEntity, String> {
}
