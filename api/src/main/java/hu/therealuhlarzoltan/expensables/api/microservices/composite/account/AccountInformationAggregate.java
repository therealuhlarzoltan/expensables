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
    private Long ownerId;
    private String currency;
    private String bankName;
    private String accountType;
    private String accountCategory;
    private Integer version;
    private BigDecimal balance;
    private List<ExpenseInfo> expenses = new ArrayList<>();
    private List<IncomeInfo> incomes = new ArrayList<>();

    public AccountInformationAggregate(AccountInformation accountInformation) {
        this.accountId = accountInformation.getAccountId();
        this.accountName = accountInformation.getAccountName();
        this.ownerId = accountInformation.getOwnerId();
        this.currency = accountInformation.getCurrency();
        this.bankName = accountInformation.getBankName();
        this.accountType = accountInformation.getAccountType();
        this.accountCategory = accountInformation.getAccountCategory();
        this.version = accountInformation.getVersion();
        this.balance = accountInformation.getBalance();
    }

}
