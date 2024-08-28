package hu.therealuhlarzoltan.expensables.microservices.account.components.mappers;

import hu.therealuhlarzoltan.expensables.microservices.account.models.AccountCategory;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface AccountCategoryMapper {

    @Named("mapAccountCategoryToString")
    default String mapAccountCategoryToString(String accountCategory) {
        return AccountCategory.fromString(accountCategory).getName();
    }

    @Named("mapStringToAccountCategory")
    default String mapStringToAccountCategory(String accountCategory) {
        return AccountCategory.fromString(accountCategory).toString();
    }
}
