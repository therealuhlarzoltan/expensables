package hu.therealuhlarzoltan.expensables.api.microservices.core.account;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Account {
    private String accountId;
    private Long ownerId;
    private String accountName;
    private String accountType;
    private String accountCategory;
    private String currency;
    private String bankName;
    private BigDecimal balance;
    private Integer version;
}
