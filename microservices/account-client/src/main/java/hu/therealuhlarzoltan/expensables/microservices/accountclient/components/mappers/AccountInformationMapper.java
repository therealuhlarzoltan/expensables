package hu.therealuhlarzoltan.expensables.microservices.accountclient.components.mappers;

import hu.therealuhlarzoltan.expensables.api.microservices.composite.account.AccountInformation;
import hu.therealuhlarzoltan.expensables.api.microservices.composite.account.AccountInformationAggregate;
import hu.therealuhlarzoltan.expensables.api.microservices.core.account.Account;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring")
public interface AccountInformationMapper {
    AccountInformation entityToApi(Account account);
    Account apiToEntity(AccountInformation accountInformation);
    default AccountInformationAggregate apiToAggregate(AccountInformation accountInformation) {
        if (accountInformation == null)
            return null;
        return new AccountInformationAggregate(accountInformation);
    };
}
