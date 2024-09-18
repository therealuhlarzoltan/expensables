package hu.therealuhlarzoltan.expensables.microservices.income.models;

import hu.therealuhlarzoltan.expensables.microservices.income.annotations.IncomeCategoryExists;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(collection = "income_records")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class IncomeRecordEntity {
    @Id
    private String id;
    @Version
    private Integer version;
    @NotNull(message = "User id is required")
    @Min(value = 1, message = "Invalid user id")
    private Long userId;
    @NotNull(message = "Account id is required")
    @Size(min = 36, max = 36, message = "Account id must be 36 characters long")
    private String accountId;
    @NotNull(message = "Income name is required")
    @Size(min = 1, max = 40, message = "Income name must be between 1 and 40 characters")
    private String name;
    @NotNull(message = "Income category is required")
    @Size(min = 3, max = 64, message = "Income category must be between 3 and 64 characters")
    @IncomeCategoryExists(message = "Invalid income category")
    private String category;
    @NotNull(message = "Amount is required")
    @Min(value = 0, message = "Amount must be greater than or equal to 0")
    private BigDecimal amount;
    @NotNull(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be 3 characters long")
    private String currency;
    private LocalDateTime timestamp;
}
