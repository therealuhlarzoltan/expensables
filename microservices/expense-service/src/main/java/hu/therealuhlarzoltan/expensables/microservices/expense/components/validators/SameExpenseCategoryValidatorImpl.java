package hu.therealuhlarzoltan.expensables.microservices.expense.components.validators;

import hu.therealuhlarzoltan.expensables.microservices.expense.models.ExpenseCategory;
import org.springframework.stereotype.Component;

@Component
public class SameExpenseCategoryValidatorImpl implements SameExpenseCategoryValidator {
    @Override
    public boolean isValid(ExpenseCategory e, String subCategory) {
        return e.getSubCategories().contains(subCategory);
    }
}
