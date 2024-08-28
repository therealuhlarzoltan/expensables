package hu.therealuhlarzoltan.expensables.microservices.expense.components.validators;

import hu.therealuhlarzoltan.expensables.microservices.expense.annotations.ExpenseCategoryExists;
import hu.therealuhlarzoltan.expensables.microservices.expense.models.ExpenseCategory;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ExpenseCategoryExistsValidator implements ConstraintValidator<ExpenseCategoryExists, String> {
    private String msg;

    @Override
    public void initialize(ExpenseCategoryExists constraintAnnotation) {
        msg = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        try {
            ExpenseCategory.valueOf(value);
            return true;
        } catch (IllegalArgumentException e) {
            context.buildConstraintViolationWithTemplate(msg).addConstraintViolation();
            return false;
        }
    }
}
