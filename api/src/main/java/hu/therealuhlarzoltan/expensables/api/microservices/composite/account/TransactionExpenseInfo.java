package hu.therealuhlarzoltan.expensables.api.microservices.composite.account;


import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class TransactionExpenseInfo extends ExpenseInfo {
    private String toAccountId;
    private String toAccountName;

    public TransactionExpenseInfo() {
        super();
        this.toAccountId = null;
        this.toAccountName = null;
    }

    public TransactionExpenseInfo(String recordId, String accountId, Long userId, String toCurrency, BigDecimal amount, Integer version, LocalDateTime expenseDate, String toAccountName, String toAccountId) {
        super(recordId, accountId, userId, "Transfer to " + toAccountName, "Outgoing transfer", "Outgoing transfer", toCurrency, amount, version, expenseDate);
        this.toAccountId = toAccountId;
        this.toAccountName = toAccountName;
    }

    public void setToAccountName(String toAccountName) {
        super.setExpenseName("Transfer to " + toAccountName);
        this.toAccountName = toAccountName;
    }




}
