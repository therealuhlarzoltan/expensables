package hu.therealuhlarzoltan.expensables.microservices.transaction.models;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionEntity {
    @Id
    private String id;
    @Version
    private Integer version;
    @NotNull(message = "From account id is required")
    @Size(min = 36, max = 36, message = "From account id must be 36 characters long")
    private String fromAccountId;
    @NotNull(message = "To account id is required")
    @Size(min = 36, max = 36, message = "To account id must be 36 characters long")
    private String toAccountId;
    @NotNull(message = "User id is required")
    @Min(value = 1, message = "Invalid user id")
    private Long userId;
    @NotNull(message = "From currency is required")
    @Size(min = 3, max = 3, message = "From currency must be 3 characters long")
    private String fromCurrency;
    @NotNull(message = "To currency is required")
    @Size(min = 3, max = 3, message = "To currency must be 3 characters long")
    private String toCurrency;
    @NotNull(message = "Amount is required")
    @Min(value = 0, message = "Amount must be greater than or equal to 0")
    private Double amount;
    private LocalDateTime timestamp;
}
