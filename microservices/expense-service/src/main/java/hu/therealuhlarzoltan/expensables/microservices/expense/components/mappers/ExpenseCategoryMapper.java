package hu.therealuhlarzoltan.expensables.microservices.expense.components.mappers;

import hu.therealuhlarzoltan.expensables.microservices.expense.models.ExpenseCategory;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface ExpenseCategoryMapper {
    @Named("mapExpenseCategoryToString")
    default String mapExpenseCategoryToString(String expenseCategory) {
        return ExpenseCategory.valueOf(expenseCategory).getName();
    };

    @Named("mapStringToExpenseCategory")
    default String mapStringToExpenseCategory(String expenseCategory) {
        String converted;
        try {
            converted = ExpenseCategory.fromString(expenseCategory).toString();
        } catch (IllegalArgumentException e) {
            converted = expenseCategory;
        }
        return converted;
    };
}
