package hu.therealuhlarzoltan.expensables.api.microservices.core.income;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

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
    private LocalDateTime incomeDate;
    private Integer version;
}
