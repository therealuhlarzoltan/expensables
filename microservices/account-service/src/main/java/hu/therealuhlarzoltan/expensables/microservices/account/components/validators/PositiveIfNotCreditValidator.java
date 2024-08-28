package hu.therealuhlarzoltan.expensables.microservices.account.components.validators;

import hu.therealuhlarzoltan.expensables.microservices.account.models.AccountEntity;

import java.math.BigDecimal;

public interface PositiveIfNotCreditValidator {
    boolean isValid(BigDecimal deduction, AccountEntity account);
}
