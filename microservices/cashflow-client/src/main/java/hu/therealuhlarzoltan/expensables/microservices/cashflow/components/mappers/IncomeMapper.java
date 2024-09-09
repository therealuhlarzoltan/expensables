package hu.therealuhlarzoltan.expensables.microservices.cashflow.components.mappers;

import hu.therealuhlarzoltan.expensables.api.microservices.composite.account.IncomeInfo;
import hu.therealuhlarzoltan.expensables.api.microservices.composite.cashflow.IncomeRecordInfo;
import hu.therealuhlarzoltan.expensables.api.microservices.core.expense.ExpenseRecord;
import hu.therealuhlarzoltan.expensables.api.microservices.core.income.IncomeRecord;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface IncomeMapper {
    IncomeRecordInfo incomeRecordToInfo(IncomeRecord incomeRecord);
    IncomeRecord incomeInfoToRecord(IncomeRecordInfo incomeRecordInfo);
}
