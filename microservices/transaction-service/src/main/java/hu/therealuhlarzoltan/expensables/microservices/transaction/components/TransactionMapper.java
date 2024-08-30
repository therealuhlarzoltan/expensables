package hu.therealuhlarzoltan.expensables.microservices.transaction.components;

import hu.therealuhlarzoltan.expensables.api.microservices.core.transaction.TransactionRecord;
import hu.therealuhlarzoltan.expensables.microservices.transaction.models.TransactionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface TransactionMapper {
    @Mappings({
            @Mapping(source = "id", target = "recordId"),
            @Mapping(source = "timestamp", target = "transactionDate")
    })
    TransactionRecord entityToApi(TransactionEntity entity);

    @Mappings({
            @Mapping(source = "recordId", target = "id"),
            @Mapping(source = "transactionDate", target = "timestamp")
    })
    TransactionEntity apiToEntity(TransactionRecord record);
}
