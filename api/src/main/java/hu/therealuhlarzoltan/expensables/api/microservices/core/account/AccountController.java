package hu.therealuhlarzoltan.expensables.api.microservices.core.account;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;
public interface AccountController {
    @Operation(summary = "Get accounts", description = "Retrieve a list of all accounts or filter by owner ID", responses = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of accounts"),
            @ApiResponse(responseCode = "404", description = "Owner not found if filtering by owner ID"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/api/accounts")
    Flux<Account> getAccounts(@Parameter(description = "Optional owner ID to filter accounts by", required = false)
                              @RequestParam(required = false) Long ownerId);

    @Operation(summary = "Get account by ID", description = "Retrieve a specific account by its ID", responses = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of the account"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @GetMapping("/api/accounts/{accountId}")
    Mono<Account> getAccountById(@Parameter(description = "UUID of the account to retrieve", required = true)
                                 @PathVariable UUID accountId);


    @Operation(summary = "Create a new account", description = "Create a new account with the provided details", responses = {
            @ApiResponse(responseCode = "201", description = "Account created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping("/api/accounts")
    Mono<Account> createAccount(@Parameter(description = "Details of the account to be created", required = true)
                                @RequestBody Account account);

    @Operation(summary = "Update an existing account", description = "Update an account with the provided details", responses = {
            @ApiResponse(responseCode = "200", description = "Account updated successfully"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @PutMapping("/api/accounts")
    Mono<Account> updateAccount(@Parameter(description = "Updated details of the account", required = true)
                                @RequestBody Account account);

    @Operation(summary = "Delete an account", description = "Delete an account by its ID", responses = {
            @ApiResponse(responseCode = "200", description = "Account deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @DeleteMapping("/api/accounts/{accountId}")
    Mono<Void> deleteAccount(@Parameter(description = "UUID of the account to delete", required = true)
                             @PathVariable UUID accountId);

    @Operation(summary = "Deposit to an account", description = "Deposit a specified amount to an account", responses = {
            @ApiResponse(responseCode = "200", description = "Amount deposited successfully"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @PutMapping("/api/accounts/{accountId}/deposit")
    Mono<Account> deposit(@Parameter(description = "UUID of the account to deposit to", required = true)
                          @PathVariable UUID accountId,
                          @Parameter(description = "Amount to deposit", required = true)
                          @RequestParam BigDecimal amount);

    @Operation(summary = "Withdraw from an account", description = "Withdraw a specified amount from an account", responses = {
            @ApiResponse(responseCode = "200", description = "Amount withdrawn successfully"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @PutMapping("/api/accounts/{accountId}/withdraw")
    Mono<Account> withdraw(@Parameter(description = "UUID of the account to withdraw from", required = true)
                           @PathVariable UUID accountId,
                           @Parameter(description = "Amount to withdraw", required = true)
                           @RequestParam BigDecimal amount);
}
