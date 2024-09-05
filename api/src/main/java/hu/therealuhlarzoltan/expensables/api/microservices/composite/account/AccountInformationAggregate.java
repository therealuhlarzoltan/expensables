package hu.therealuhlarzoltan.expensables.api.microservices.composite.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountInformationAggregate {
    private String accountId;
    private String accountName;
    private int ownerId;
    private String currency;
    private String bankName;
    private String accountType;
    private String accountCategory;
    private Integer version;
    private BigDecimal balance;
    private List<? extends ExpenseInfo> expenses;
    private List<? extends IncomeInfo> incomes;

}
