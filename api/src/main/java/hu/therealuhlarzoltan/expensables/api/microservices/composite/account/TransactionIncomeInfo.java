package hu.therealuhlarzoltan.expensables.api.microservices.composite.account;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Getter
@Setter
public class TransactionIncomeInfo extends IncomeInfo {
    private String fromAccountId;
    private String fromAccountName;

    public TransactionIncomeInfo() {
        super();
        this.fromAccountId = null;
        this.fromAccountName = null;
    }

    public TransactionIncomeInfo(String recordId, String accountId, Long userId, BigDecimal amount, Integer version, ZonedDateTime incomeDate, String fromAccountId, String fromCurrency, String fromAccountName) {
        super(recordId, accountId, userId, "Transfer from " + fromAccountName, "Incoming transfer", fromCurrency, amount, version, incomeDate);
        this.fromAccountId = fromAccountId;
        this.fromAccountName = fromAccountName;
    }

    public void setFromAccountName(String fromAccountName) {
        super.setIncomeName("Transfer from " + fromAccountName);
        this.fromAccountName = fromAccountName;
    }
}
