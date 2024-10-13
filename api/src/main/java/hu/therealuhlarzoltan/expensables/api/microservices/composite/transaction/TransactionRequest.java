package hu.therealuhlarzoltan.expensables.api.microservices.composite.transaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor(force = true)
public class TransactionRequest {
    private Long userId;
    private final String fromAccountId;
    private final String toAccountId;
    private final BigDecimal amount;
}
