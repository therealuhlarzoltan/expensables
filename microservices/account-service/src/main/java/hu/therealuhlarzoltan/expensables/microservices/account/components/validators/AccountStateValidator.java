package hu.therealuhlarzoltan.expensables.microservices.account.components.validators;

import hu.therealuhlarzoltan.expensables.microservices.account.models.AccountEntity;

public interface AccountStateValidator {
    void validate(AccountEntity account);
}
