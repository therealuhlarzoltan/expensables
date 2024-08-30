package hu.therealuhlarzoltan.expensables.microservices.income.components.mappers;

import hu.therealuhlarzoltan.expensables.api.microservices.core.income.IncomeRecord;
import hu.therealuhlarzoltan.expensables.microservices.income.models.IncomeRecordEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring", uses = {IncomeCategoryMapper.class})
public interface IncomeRecordMapper {
    @Mappings({
            @Mapping(source = "id", target = "recordId"),
            @Mapping(source = "userId", target = "userId"),
            @Mapping(source = "accountId", target = "accountId"),
            @Mapping(source = "amount", target = "amount"),
            @Mapping(source = "currency", target = "currency"),
            @Mapping(source = "name", target = "incomeName"),
            @Mapping(source = "version", target = "version"),
            @Mapping(source = "timestamp", target = "incomeDate"),
            @Mapping(source = "category", target = "incomeCategory", qualifiedByName = "mapIncomeCategoryToString")
    })
    IncomeRecord entityToIncomeRecord(IncomeRecordEntity incomeRecordEntity);
    @Mappings({
            @Mapping(source = "recordId", target = "id"),
            @Mapping(source = "userId", target = "userId"),
            @Mapping(source = "accountId", target = "accountId"),
            @Mapping(source = "amount", target = "amount"),
            @Mapping(source = "currency", target = "currency"),
            @Mapping(source = "incomeName", target = "name"),
            @Mapping(source = "version", target = "version"),
            @Mapping(source = "incomeDate", target = "timestamp"),
            @Mapping(source = "incomeCategory", target = "category", qualifiedByName = "mapStringToIncomeCategory"),
    })
    IncomeRecordEntity incomeRecordToEntity(IncomeRecord incomeRecord);
}
