package hu.therealuhlarzoltan.expensables.api.microservices.core.transaction;

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
public class TransactionRecord {
    private String recordId;
    private Integer version;
    private Long userId;
    private String fromAccountId;
    private String toAccountId;
    private BigDecimal amount;
    private String fromCurrency;
    private String toCurrency;
    private ZonedDateTime transactionDate;
}
