package hu.therealuhlarzoltan.expensables.api.microservices.composite.account;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Getter
@Setter
public class IncomeInfo {
    private String recordId;
    private String accountId;
    private Long userId;
    private String incomeName;
    private String incomeCategory;
    private String currency;
    private BigDecimal amount;
    private Integer version;
    private ZonedDateTime incomeDate;

    public IncomeInfo() {
        this.recordId = null;
        this.accountId = null;
        this.userId = null;
        this.incomeName = null;
        this.incomeCategory = null;
        this.currency = null;
        this.amount = null;
        this.version = null;
        this.incomeDate = null;
    }

    public IncomeInfo(String recordId, String accountId, Long userId, String incomeName, String incomeCategory, String currency, BigDecimal amount, Integer version, ZonedDateTime incomeDate) {
        this.recordId = recordId;
        this.accountId = accountId;
        this.userId = userId;
        this.incomeName = incomeName;
        this.incomeCategory = incomeCategory;
        this.currency = currency;
        this.amount = amount;
        this.version = version;
        this.incomeDate = incomeDate;
    }
}
