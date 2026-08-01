package com.diligent.expensetracker.controller;

import com.diligent.expensetracker.dto.CategorySummaryResponse;
import com.diligent.expensetracker.dto.ExpenseRequest;
import com.diligent.expensetracker.dto.ExpenseResponse;
import com.diligent.expensetracker.dto.ExpenseSummaryResponse;
import com.diligent.expensetracker.dto.MonthlySummaryResponse;
import com.diligent.expensetracker.enums.ExpenseCategory;
import com.diligent.expensetracker.response.ApiResponse;
import com.diligent.expensetracker.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "Expense API",
        description = "REST APIs for managing expenses"
)
@RestController
@RequestMapping("/api/v1/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @Operation(summary = "Create a new expense")
    @PostMapping
    public ResponseEntity<ApiResponse<ExpenseResponse>> createExpense(
            @Valid @RequestBody ExpenseRequest request
    ) {

        ExpenseResponse expense = expenseService.createExpense(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Expense created successfully",
                                expense
                        )
                );
    }

    @Operation(summary = "Retrieve all expenses")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ExpenseResponse>>> getAllExpenses() {

        List<ExpenseResponse> expenses = expenseService.getAllExpenses();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Expenses retrieved successfully",
                        expenses
                )
        );
    }

    @Operation(summary = "Retrieve an expense by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ExpenseResponse>> getExpenseById(
            @PathVariable Long id
    ) {

        ExpenseResponse expense = expenseService.getExpenseById(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Expense retrieved successfully",
                        expense
                )
        );
    }

    @Operation(summary = "Update an existing expense")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ExpenseResponse>> updateExpense(
            @PathVariable Long id,
            @Valid @RequestBody ExpenseRequest request
    ) {

        ExpenseResponse expense = expenseService.updateExpense(id, request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Expense updated successfully",
                        expense
                )
        );
    }

    @Operation(summary = "Delete an expense")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteExpense(
            @PathVariable Long id
    ) {

        expenseService.deleteExpense(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Expense deleted successfully",
                        null
                )
        );
    }

    @Operation(summary = "Filter expenses by category")
    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<List<ExpenseResponse>>> getExpensesByCategory(
            @PathVariable ExpenseCategory category
    ) {

        List<ExpenseResponse> expenses =
                expenseService.getExpensesByCategory(category);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Expenses retrieved successfully",
                        expenses
                )
        );
    }

    @Operation(summary = "Search expenses by title")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ExpenseResponse>>> searchExpenses(
            @RequestParam String keyword
    ) {

        List<ExpenseResponse> expenses =
                expenseService.searchExpenses(keyword);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Search completed successfully",
                        expenses
                )
        );
    }

    @Operation(summary = "Retrieve expense summary")
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<ExpenseSummaryResponse>> getExpenseSummary() {

        ExpenseSummaryResponse summary =
                expenseService.getExpenseSummary();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Expense summary retrieved successfully",
                        summary
                )
        );
    }

    @Operation(summary = "Retrieve monthly expense summary")
    @GetMapping("/monthly-summary")
    public ResponseEntity<ApiResponse<List<MonthlySummaryResponse>>> getMonthlySummary() {

        List<MonthlySummaryResponse> summary =
                expenseService.getMonthlySummary();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Monthly summary retrieved successfully",
                        summary
                )
        );
    }

    @Operation(summary = "Retrieve category-wise expense summary")
    @GetMapping("/summary/by-category")
    public ResponseEntity<ApiResponse<List<CategorySummaryResponse>>> getCategorySummary() {

        List<CategorySummaryResponse> summary =
                expenseService.getCategorySummary();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Category summary retrieved successfully",
                        summary
                )
        );
    }
}