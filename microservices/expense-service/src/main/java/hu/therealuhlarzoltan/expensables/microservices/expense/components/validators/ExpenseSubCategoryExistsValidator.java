package hu.therealuhlarzoltan.expensables.microservices.expense.components.validators;

import hu.therealuhlarzoltan.expensables.microservices.expense.annotations.ExpenseSubCategoryExists;
import hu.therealuhlarzoltan.expensables.microservices.expense.models.ExpenseCategory;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

public class ExpenseSubCategoryExistsValidator implements ConstraintValidator<ExpenseSubCategoryExists, String> {
    private String msg;

    @Override
    public void initialize(ExpenseSubCategoryExists constraintAnnotation) {
        this.msg = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        List<String> allSubCategories = ExpenseCategory.getAllSubCategories();
        if (allSubCategories.contains(value)) {
            return true;
        } else {
            context.buildConstraintViolationWithTemplate(msg).addConstraintViolation();
            return false;
        }
    }
}
