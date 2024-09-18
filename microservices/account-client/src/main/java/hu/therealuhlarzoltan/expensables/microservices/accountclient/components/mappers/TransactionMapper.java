package hu.therealuhlarzoltan.expensables.microservices.accountclient.components.mappers;

import hu.therealuhlarzoltan.expensables.api.microservices.composite.account.TransactionExpenseInfo;
import hu.therealuhlarzoltan.expensables.api.microservices.composite.account.TransactionIncomeInfo;
import hu.therealuhlarzoltan.expensables.api.microservices.core.transaction.TransactionRecord;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TransactionMapper {
    default TransactionExpenseInfo expenseTransactionToApi(TransactionRecord transactionRecord) {
        if (transactionRecord == null)
            return null;
        return new TransactionExpenseInfo(
                transactionRecord.getRecordId(),
                transactionRecord.getFromAccountId(),
                transactionRecord.getUserId(),
                transactionRecord.getToCurrency(),
                transactionRecord.getAmount(),
                transactionRecord.getVersion(),
                transactionRecord.getTransactionDate(),
                "",
                transactionRecord.getToAccountId()
        );
    };

    default TransactionIncomeInfo incomeTransactionToApi(TransactionRecord transactionRecord) {
        if (transactionRecord == null)
            return null;
        return new TransactionIncomeInfo(
                transactionRecord.getRecordId(),
                transactionRecord.getToAccountId(),
                transactionRecord.getUserId(),
                transactionRecord.getAmount(),
                transactionRecord.getVersion(),
                transactionRecord.getTransactionDate(),
                transactionRecord.getFromAccountId(),
                transactionRecord.getFromCurrency(),
                "");
    };
}
