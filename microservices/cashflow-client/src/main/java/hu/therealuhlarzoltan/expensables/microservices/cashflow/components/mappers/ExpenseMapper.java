package hu.therealuhlarzoltan.expensables.microservices.cashflow.components.mappers;

import hu.therealuhlarzoltan.expensables.api.microservices.composite.account.ExpenseInfo;
import hu.therealuhlarzoltan.expensables.api.microservices.composite.account.IncomeInfo;
import hu.therealuhlarzoltan.expensables.api.microservices.composite.cashflow.ExpenseRecordInfo;
import hu.therealuhlarzoltan.expensables.api.microservices.core.expense.ExpenseRecord;
import hu.therealuhlarzoltan.expensables.api.microservices.core.income.IncomeRecord;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ExpenseMapper {
    ExpenseRecordInfo expenseRecordToInfo(ExpenseRecord expenseRecord);
    ExpenseRecord expenseInfoToRecord(ExpenseRecordInfo expenseInfo);
}
