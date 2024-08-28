package hu.therealuhlarzoltan.expensables.microservices.account.components.mappers;

import hu.therealuhlarzoltan.expensables.microservices.account.models.AccountType;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface AccountTypeMapper {


    @Named("mapAccountTypeToString")
    default String mapAccountTypeToString(String accountType) {
        return AccountType.fromString(accountType).getName();
    }

    @Named("mapStringToAccountType")
    default String mapStringToAccountType(String accountType) {
        return AccountType.fromString(accountType).toString();
    }
}
