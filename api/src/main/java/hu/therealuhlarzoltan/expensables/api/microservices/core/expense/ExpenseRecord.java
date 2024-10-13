package hu.therealuhlarzoltan.expensables.api.microservices.core.expense;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.ZonedDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExpenseRecord {
    private String recordId;
    private Long userId;
    private String accountId;
    private String expenseName;
    private String expenseCategory;
    private String expenseSubCategory;
    private BigDecimal amount;
    private String currency;
    private ZonedDateTime expenseDate;
    private Integer version;
}
