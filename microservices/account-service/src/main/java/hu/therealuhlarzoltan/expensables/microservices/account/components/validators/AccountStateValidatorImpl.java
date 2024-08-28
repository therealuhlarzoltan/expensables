package hu.therealuhlarzoltan.expensables.microservices.account.components.validators;

import hu.therealuhlarzoltan.expensables.api.microservices.exceptions.InvalidInputDataException;
import hu.therealuhlarzoltan.expensables.microservices.account.models.AccountCategory;
import hu.therealuhlarzoltan.expensables.microservices.account.models.AccountEntity;
import hu.therealuhlarzoltan.expensables.microservices.account.models.AccountType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class AccountStateValidatorImpl implements AccountStateValidator {
    @Override
    public void validate(AccountEntity account) {
        if (account.getType().equals(AccountType.PHYSICAL.toString())) {
            if (!account.getCategory().equals(AccountCategory.CHECKING.toString()) && !account.getCategory().equals(AccountCategory.SAVINGS.toString())) {
                throw new InvalidInputDataException("Invalid account category for physical account: " + account.getCategory());
            }
            if (account.getBalance().compareTo(BigDecimal.ZERO) < 0) {
                throw new InvalidInputDataException("Invalid balance for physical account: " + account.getBalance());
            }
            if (account.getBank() != null) {
                throw new InvalidInputDataException("Bank is not required for physical account");
            }
        } else {
            if (account.getBank() == null || account.getBank().isBlank()) {
                throw new InvalidInputDataException("Bank is required for virtual account");
            }
            if (account.getBalance().compareTo(BigDecimal.ZERO) < 0 && !account.getType().equals(AccountType.CREDIT.toString())) {
                throw new InvalidInputDataException("Balance cannot be negative for a non-credit account: " + account.getBalance());
            }
        }
    }
}
