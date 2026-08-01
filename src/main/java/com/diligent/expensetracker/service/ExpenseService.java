package com.diligent.expensetracker.service;

import com.diligent.expensetracker.dto.CategorySummaryResponse;
import com.diligent.expensetracker.dto.ExpenseRequest;
import com.diligent.expensetracker.dto.ExpenseResponse;
import com.diligent.expensetracker.dto.ExpenseSummaryResponse;
import com.diligent.expensetracker.dto.MonthlySummaryResponse;
import com.diligent.expensetracker.enums.ExpenseCategory;

import java.util.List;

public interface ExpenseService {

    ExpenseResponse createExpense(ExpenseRequest request);

    List<ExpenseResponse> getAllExpenses();

    ExpenseResponse getExpenseById(Long id);

    ExpenseResponse updateExpense(Long id, ExpenseRequest request);

    void deleteExpense(Long id);

    List<ExpenseResponse> getExpensesByCategory(ExpenseCategory category);

    List<ExpenseResponse> searchExpenses(String keyword);

    ExpenseSummaryResponse getExpenseSummary();

    List<MonthlySummaryResponse> getMonthlySummary();

    List<CategorySummaryResponse> getCategorySummary();
}