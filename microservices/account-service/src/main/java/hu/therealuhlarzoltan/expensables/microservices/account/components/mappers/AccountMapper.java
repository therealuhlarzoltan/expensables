package hu.therealuhlarzoltan.expensables.microservices.account.components.mappers;

import hu.therealuhlarzoltan.expensables.api.microservices.core.account.Account;
import hu.therealuhlarzoltan.expensables.microservices.account.models.AccountEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring", uses = {AccountCategoryMapper.class, AccountTypeMapper.class})
public interface AccountMapper {

    @Mappings({
            @Mapping(source = "entityId", target = "accountId"),
            @Mapping(source = "name", target = "accountName"),
            @Mapping(source = "type", target = "accountType", qualifiedByName = "mapAccountTypeToString"),
            @Mapping(source = "category", target = "accountCategory", qualifiedByName = "mapAccountCategoryToString"),
            @Mapping(source = "currency", target = "currency"),
            @Mapping(source = "bank", target = "bankName"),
            @Mapping(source = "balance", target = "balance"),
            @Mapping(source= "version", target = "version")
    })
    Account accountEntityToAccount(AccountEntity accountEntity);

    @Mappings({
            @Mapping(source = "accountId", target = "entityId"),
            @Mapping(source = "accountName", target = "name"),
            @Mapping(source = "accountType", target = "type", qualifiedByName = "mapStringToAccountType"),
            @Mapping(source = "accountCategory", target = "category", qualifiedByName = "mapStringToAccountCategory"),
            @Mapping(source = "currency", target = "currency"),
            @Mapping(source = "bankName", target = "bank"),
            @Mapping(source = "balance", target = "balance"),
            @Mapping(source= "version", target = "version"),
    })
    AccountEntity accountToAccountEntity(Account account);
}
