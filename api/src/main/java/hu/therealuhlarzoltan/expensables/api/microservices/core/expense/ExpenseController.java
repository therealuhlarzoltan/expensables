package hu.therealuhlarzoltan.expensables.api.microservices.core.expense;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ExpenseController {

    @Operation(summary = "Get all expenses", description = "Retrieve all expense records")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of all expenses"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping(
            value = "/api/expenses",
            produces = "application/json"
    )
    Flux<ExpenseRecord> getAllExpenses();

    @Operation(summary = "Get an expense by ID", description = "Retrieve a specific expense record by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of the expense"),
            @ApiResponse(responseCode = "404", description = "Expense not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping(
            value = "/api/expenses/get/{recordId}",
            produces = "application/json"
    )
    Mono<ExpenseRecord> getExpense(
            @Parameter(description = "ID of the expense record to be retrieved")
            @PathVariable String recordId
    );

    @Operation(summary = "Get expenses by account ID", description = "Retrieve all expenses for a specific account by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of the expenses"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping(
            value = "/api/expenses/{accountId}",
            produces = "application/json"
    )
    Flux<ExpenseRecord> getExpensesByAccount(
            @Parameter(description = "ID of the account for which to retrieve expenses")
            @PathVariable String accountId
    );

    @Operation(summary = "Create a new expense", description = "Create a new expense record")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Expense successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping(
            value = "/api/expenses",
            produces = "application/json",
            consumes = "application/json"
    )
    Mono<ExpenseRecord> createExpense(
            @Parameter(description = "Expense record to be created")
            @Valid @RequestBody ExpenseRecord expenseRecord
    );

    @Operation(summary = "Update an existing expense", description = "Update an existing expense record")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Expense successfully updated"),
            @ApiResponse(responseCode = "404", description = "Expense not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping(
            value = "/api/expenses",
            produces = "application/json",
            consumes = "application/json"
    )
    Mono<ExpenseRecord> updateExpense(
            @Parameter(description = "Expense record with updated information")
            @Valid @RequestBody ExpenseRecord expenseRecord
    );

    @Operation(summary = "Delete an expense by ID", description = "Delete a specific expense record by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Expense successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Expense not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping(
            value = "/api/expenses/{recordId}",
            produces = "application/json"
    )
    Mono<Void> deleteExpense(
            @Parameter(description = "ID of the expense record to be deleted")
            @PathVariable String recordId
    );
    @DeleteMapping(
            value = "/api/expenses",
            produces = "application/json"
    )
    Mono<Void> deleteExpensesByAccount(
            @Parameter(description = "ID of the account for which to delete expenses")
            @RequestParam String accountId
    );
}
