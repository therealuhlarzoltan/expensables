package hu.therealuhlarzoltan.expensables.microservices.account.components.validators;

import hu.therealuhlarzoltan.expensables.microservices.account.annotations.CurrencyExists;
import hu.therealuhlarzoltan.expensables.microservices.account.models.Currency;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CurrencyExistsValidator implements ConstraintValidator<CurrencyExists, String> {
    private String msg;

    @Override
    public void initialize(CurrencyExists constraintAnnotation) {
        msg = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        try {
            Currency.valueOf(value);
        } catch (IllegalArgumentException e) {
            context.buildConstraintViolationWithTemplate(msg).addConstraintViolation();
            return false;
        }
        return true;
    }
}
