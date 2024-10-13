package hu.therealuhlarzoltan.expensables.microservices.income.components.validators;

import hu.therealuhlarzoltan.expensables.microservices.income.annotations.IncomeCategoryExists;
import hu.therealuhlarzoltan.expensables.microservices.income.models.IncomeCategory;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class IncomeCategoryExistsValidator implements ConstraintValidator<IncomeCategoryExists, String> {
    private String msg;

    @Override
    public void initialize(IncomeCategoryExists constraintAnnotation) {
        msg = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        try {
            IncomeCategory.valueOf(value);
            return true;
        } catch (IllegalArgumentException e) {
            context.buildConstraintViolationWithTemplate(msg).addConstraintViolation();
            return false;
        }
    }
}
