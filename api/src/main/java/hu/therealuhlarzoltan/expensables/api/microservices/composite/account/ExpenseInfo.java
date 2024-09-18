package hu.therealuhlarzoltan.expensables.api.microservices.composite.account;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class ExpenseInfo {
    private String recordId;
    private String accountId;
    private Long userId;
    private String expenseName;
    private String expenseCategory;
    private String expenseSubCategory;
    private String currency;
    private BigDecimal amount;
    private Integer version;
    private LocalDateTime expenseDate;

    public ExpenseInfo() {
        this.recordId = null;
        this.accountId = null;
        this.userId = null;
        this.expenseName = null;
        this.expenseCategory = null;
        this.expenseSubCategory = null;
        this.currency = null;
        this.amount = null;
        this.version = null;
        this.expenseDate = null;
    }

    public ExpenseInfo(String recordId, String accountId, Long userId, String expenseName, String expenseCategory, String expenseSubCategory, String currency, BigDecimal amount, Integer version, LocalDateTime expenseDate) {
        this.recordId = recordId;
        this.accountId = accountId;
        this.userId = userId;
        this.expenseName = expenseName;
        this.expenseCategory = expenseCategory;
        this.expenseSubCategory = expenseSubCategory;
        this.currency = currency;
        this.amount = amount;
        this.version = version;
        this.expenseDate = expenseDate;
    }
}
