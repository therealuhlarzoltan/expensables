package hu.therealuhlarzoltan.expensables.microservices.account.components.validators;

import hu.therealuhlarzoltan.expensables.microservices.account.annotations.AccountTypeExists;
import hu.therealuhlarzoltan.expensables.microservices.account.models.AccountType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class AccountTypeExistsValidator implements ConstraintValidator<AccountTypeExists, String> {
    private String msg;

    @Override
    public void initialize(AccountTypeExists constraintAnnotation) {
        msg = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        try {
            AccountType.valueOf(value);
        } catch (IllegalArgumentException e) {
            context.buildConstraintViolationWithTemplate(msg).addConstraintViolation();
            return false;
        }
        return true;
    }
}
