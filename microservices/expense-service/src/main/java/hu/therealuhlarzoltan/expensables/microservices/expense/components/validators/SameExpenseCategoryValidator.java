package hu.therealuhlarzoltan.expensables.microservices.expense.components.validators;

import hu.therealuhlarzoltan.expensables.microservices.expense.models.ExpenseCategory;

public interface SameExpenseCategoryValidator {
    boolean isValid(ExpenseCategory e, String subCategory);
}
