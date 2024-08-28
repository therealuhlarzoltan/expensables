package hu.therealuhlarzoltan.expensables.microservices.account.models;

import hu.therealuhlarzoltan.expensables.microservices.account.annotations.AccountCategoryExists;
import hu.therealuhlarzoltan.expensables.microservices.account.annotations.AccountTypeExists;
import hu.therealuhlarzoltan.expensables.microservices.account.annotations.CurrencyExists;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;


import java.math.BigDecimal;

@Document(collection = "bank_accounts")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountEntity {
    @Id
    private String id;

    @Indexed(unique = true)
    @NotNull(message = "Entity id is required")
    @Size(min = 36, max = 36, message = "Id must be 36 characters long")
    private String entityId;

    @Version
    private Integer version;

    @NotNull(message = "Owner id is required")
    @Min(value = 1, message = "Invalid owner id")
    private Long ownerId;

    @NotNull(message = "Account name is required")
    @Size(min = 1, max = 40, message = "Account type must be between 1 and 40 characters")
    private String name;


    @Size(max = 40, message = "Bank name must be maximum 40 characters long")
    private String bank;

    @NotNull(message = "Balance is required")
    private BigDecimal balance;

    @NotNull(message = "Account type is required")
    @Size(min = 3, max = 20, message = "Account type must be between 3 and 20 characters")
    @AccountTypeExists
    private String type;

    @NotNull(message = "Account category is required")
    @Size(min = 3, max = 20, message = "Account category must be between 3 and 20 characters")
    @AccountCategoryExists
    private String category;

    @NotNull(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be 3 characters long")
    @CurrencyExists
    private String currency;

}
