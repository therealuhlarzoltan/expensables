package hu.therealuhlarzoltan.expensables.api.microservices.composite.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class TransactionExpenseInfo extends ExpenseInfo {
    private String recordId;
}
