package hu.therealuhlarzoltan.expensables.microservices.account.components.validators;

import hu.therealuhlarzoltan.expensables.microservices.account.models.AccountEntity;
import hu.therealuhlarzoltan.expensables.microservices.account.models.AccountType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PositiveIfNotCreditValidatorImpl implements PositiveIfNotCreditValidator {
    @Override
    public boolean isValid(BigDecimal deduction, AccountEntity account) {
        if (account.getType().equals(AccountType.CREDIT.toString())) {
            return true;
        } else {
            return account.getBalance().compareTo(deduction) >= 0;
        }
    }
}
