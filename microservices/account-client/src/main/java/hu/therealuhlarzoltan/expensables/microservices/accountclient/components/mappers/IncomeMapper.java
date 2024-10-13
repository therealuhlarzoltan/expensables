package hu.therealuhlarzoltan.expensables.microservices.accountclient.components.mappers;

import hu.therealuhlarzoltan.expensables.api.microservices.composite.account.IncomeInfo;
import hu.therealuhlarzoltan.expensables.api.microservices.core.income.IncomeRecord;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface IncomeMapper {
    IncomeInfo entityToApi(IncomeRecord incomeRecord);
}
