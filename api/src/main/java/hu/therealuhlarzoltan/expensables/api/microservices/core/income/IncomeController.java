package hu.therealuhlarzoltan.expensables.api.microservices.core.income;

import hu.therealuhlarzoltan.expensables.api.microservices.core.expense.ExpenseRecord;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IncomeController {
    @Operation(summary = "Get all incomes", description = "Retrieve all income records")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of all incomes"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping(
            value = "/api/incomes",
            produces = "application/json"
    )
    Flux<IncomeRecord> getAllIncomes();

    @Operation(summary = "Get an income by ID", description = "Retrieve a specific income record by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of the income"),
            @ApiResponse(responseCode = "404", description = "Income not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping(
            value = "/api/incomes/get/{recordId}",
            produces = "application/json"
    )
    Mono<IncomeRecord> getIncome(
            @Parameter(description = "ID of the income record to be retrieved")
            @PathVariable String recordId
    );

    @Operation(summary = "Get incomes by account ID", description = "Retrieve all incomes for a specific account by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful retrieval of the incomes"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping(
            value = "/api/incomes/{accountId}",
            produces = "application/json"
    )
    Flux<IncomeRecord> getIncomesByAccount(
            @Parameter(description = "ID of the account for which to retrieve incomes")
            @PathVariable String accountId
    );

    @Operation(summary = "Create a new income", description = "Create a new income record")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Income successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping(
            value = "/api/incomes",
            produces = "application/json",
            consumes = "application/json"
    )
    Mono<IncomeRecord> createIncome(
            @Parameter(description = "Income record to be created")
            @Valid @RequestBody IncomeRecord incomeRecord
    );

    @Operation(summary = "Update an existing income", description = "Update an existing income record")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Income successfully updated"),
            @ApiResponse(responseCode = "404", description = "Income not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping(
            value = "/api/incomes",
            produces = "application/json",
            consumes = "application/json"
    )
    Mono<IncomeRecord> updateIncome(
            @Parameter(description = "Income record with updated information")
            @Valid @RequestBody IncomeRecord incomeRecord
    );

    @Operation(summary = "Delete an income by ID", description = "Delete a specific income record by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Income successfully deleted"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping(
            value = "/api/incomes/{recordId}",
            produces = "application/json"
    )
    Mono<Void> deleteIncome(
            @Parameter(description = "ID of the income record to be deleted")
            @PathVariable String recordId
    );
}
