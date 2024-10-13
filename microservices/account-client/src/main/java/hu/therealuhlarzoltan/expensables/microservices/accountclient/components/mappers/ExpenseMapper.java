package hu.therealuhlarzoltan.expensables.microservices.accountclient.components.mappers;

import hu.therealuhlarzoltan.expensables.api.microservices.composite.account.ExpenseInfo;
import hu.therealuhlarzoltan.expensables.api.microservices.composite.account.TransactionExpenseInfo;
import hu.therealuhlarzoltan.expensables.api.microservices.composite.account.TransactionIncomeInfo;
import hu.therealuhlarzoltan.expensables.api.microservices.core.expense.ExpenseRecord;
import hu.therealuhlarzoltan.expensables.api.microservices.core.transaction.TransactionRecord;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ExpenseMapper {
    ExpenseInfo entityToApi(ExpenseRecord expenseRecord);
}
