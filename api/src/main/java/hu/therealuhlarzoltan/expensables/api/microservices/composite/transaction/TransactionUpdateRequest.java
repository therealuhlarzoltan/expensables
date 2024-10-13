package hu.therealuhlarzoltan.expensables.api.microservices.composite.transaction;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor(force = true)
public class TransactionUpdateRequest {
    private final Integer version;
    private final BigDecimal amount;
}
