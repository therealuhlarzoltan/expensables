package hu.therealuhlarzoltan.expensables.api.microservices.core.exchange;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExchangeResponse {
    private String fromCurrency;
    private String toCurrency;
    private BigDecimal exchangeRate;
    private BigDecimal amount;
    private BigDecimal result;
    private ZonedDateTime exchangeDate;
}
