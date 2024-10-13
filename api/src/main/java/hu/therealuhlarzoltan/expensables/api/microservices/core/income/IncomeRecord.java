package hu.therealuhlarzoltan.expensables.api.microservices.core.income;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncomeRecord {
    private String recordId;
    private Long userId;
    private String accountId;
    private String incomeName;
    private String incomeCategory;
    private BigDecimal amount;
    private String currency;
    private ZonedDateTime incomeDate;
    private Integer version;
}
