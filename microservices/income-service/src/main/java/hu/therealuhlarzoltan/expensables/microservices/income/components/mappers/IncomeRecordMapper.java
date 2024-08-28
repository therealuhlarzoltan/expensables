package hu.therealuhlarzoltan.expensables.microservices.income.components.mappers;

import hu.therealuhlarzoltan.expensables.microservices.income.models.IncomeRecordEntity;

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
    IncomeRecord entityToIncomeRecord(ExpenseRecordEntity expenseRecordEntity);
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
