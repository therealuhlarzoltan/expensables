package hu.therealuhlarzoltan.expensables.microservices.expense.components.mappers;

import hu.therealuhlarzoltan.expensables.api.microservices.core.expense.ExpenseRecord;
import hu.therealuhlarzoltan.expensables.microservices.expense.models.ExpenseRecordEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;


@Mapper(componentModel = "spring", uses = {ExpenseCategoryMapper.class, ExpenseSubCategoryMapper.class})
public interface ExpenseRecordMapper {

    @Mappings({
            @Mapping(source = "id", target = "recordId"),
            @Mapping(source = "userId", target = "userId"),
            @Mapping(source = "accountId", target = "accountId"),
            @Mapping(source = "amount", target = "amount"),
            @Mapping(source = "currency", target = "currency"),
            @Mapping(source = "name", target = "expenseName"),
            @Mapping(source = "version", target = "version"),
            @Mapping(source = "timestamp", target = "expenseDate"),
            @Mapping(source = "subCategory", target = "expenseSubCategory", qualifiedByName = "mapSubCategoryToString"),
            @Mapping(source = "category", target = "expenseCategory", qualifiedByName = "mapExpenseCategoryToString")
    })
    ExpenseRecord entityToExpenseRecord(ExpenseRecordEntity expenseRecordEntity);
    @Mappings({
            @Mapping(source = "recordId", target = "id"),
            @Mapping(source = "userId", target = "userId"),
            @Mapping(source = "accountId", target = "accountId"),
            @Mapping(source = "amount", target = "amount"),
            @Mapping(source = "currency", target = "currency"),
            @Mapping(source = "expenseName", target = "name"),
            @Mapping(source = "version", target = "version"),
            @Mapping(source = "expenseDate", target = "timestamp"),
            @Mapping(source = "expenseCategory", target = "category", qualifiedByName = "mapStringToExpenseCategory"),
            @Mapping(source = "expenseSubCategory", target = "subCategory", qualifiedByName = "mapStringToSubCategory")
    })
    ExpenseRecordEntity expenseRecordToEntity(ExpenseRecord expenseRecord);
}
