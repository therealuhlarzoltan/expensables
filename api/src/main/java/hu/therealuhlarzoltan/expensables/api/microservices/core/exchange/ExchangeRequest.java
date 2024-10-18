package hu.therealuhlarzoltan.expensables.api.microservices.core.exchange;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExchangeRequest {
    @NotNull(message = "From currency is required")
    @Size(min = 3, max = 3, message = "From currency must be 3 characters long")
    private String fromCurrency;
    @NotNull(message = "To currency is required")
    @Size(min = 3, max = 3, message = "To currency must be 3 characters long")
    private String toCurrency;
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than 0")
    private BigDecimal amount;
    private ZonedDateTime exchangeDate;
}
