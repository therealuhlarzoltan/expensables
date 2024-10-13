package hu.therealuhlarzoltan.expensables.microservices.account.components.validators;

import hu.therealuhlarzoltan.expensables.microservices.account.annotations.AccountCategoryExists;
import hu.therealuhlarzoltan.expensables.microservices.account.models.AccountCategory;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;


public class AccountCategoryExistsValidator implements ConstraintValidator<AccountCategoryExists, String> {

    private String msg;

    @Override
    public void initialize(AccountCategoryExists constraintAnnotation) {
        msg = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        try {
            AccountCategory.fromString(value);
        } catch (IllegalArgumentException e) {
            context.buildConstraintViolationWithTemplate(msg).addConstraintViolation();
            return false;
        }
        return true;
    }
}
