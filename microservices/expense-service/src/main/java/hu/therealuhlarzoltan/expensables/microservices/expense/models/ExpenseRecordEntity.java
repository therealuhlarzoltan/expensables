package hu.therealuhlarzoltan.expensables.microservices.expense.models;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.ZonedDateTimeSerializer;
import hu.therealuhlarzoltan.expensables.microservices.expense.annotations.ExpenseCategoryExists;
import hu.therealuhlarzoltan.expensables.microservices.expense.annotations.ExpenseSubCategoryExists;
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
import java.time.ZonedDateTime;

@Document(collection = "expense_records")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExpenseRecordEntity {
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
    @NotNull(message = "Expense name is required")
    @Size(min = 1, max = 40, message = "Expense name must be between 1 and 40 characters")
    private String name;
    @NotNull(message = "Expense category is required")
    @Size(min = 3, max = 64, message = "Expense category must be between 3 and 64 characters")
    @ExpenseCategoryExists(message = "Invalid expense category")
    private String category;
    @NotNull(message = "Expense subcategory is required")
    @Size(min = 3, max = 64, message = "Expense subcategory must be between 3 and 64 characters")
    @ExpenseSubCategoryExists(message = "Invalid expense subcategory")
    private String subCategory;
    @NotNull(message = "Amount is required")
    @Min(value = 0, message = "Amount must be greater than or equal to 0")
    private BigDecimal amount;
    @NotNull(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be 3 characters long")
    private String currency;
    private ZonedDateTime timestamp;
}
