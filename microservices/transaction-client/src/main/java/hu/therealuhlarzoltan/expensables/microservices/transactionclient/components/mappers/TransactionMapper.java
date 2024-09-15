package hu.therealuhlarzoltan.expensables.microservices.transactionclient.components.mappers;

import hu.therealuhlarzoltan.expensables.api.microservices.composite.transaction.TransactionInfo;
import hu.therealuhlarzoltan.expensables.api.microservices.core.transaction.TransactionRecord;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface TransactionMapper {
    @Mappings({
            @Mapping(target = "recordId", source = "transactionId"),
    })
    TransactionRecord transactionInfoToRecord(TransactionInfo transactionInfo);
    @Mappings({
            @Mapping(target = "transactionId", source = "recordId"),
    })
    TransactionInfo transactionRecordToInfo(TransactionRecord transactionRecord);
}
