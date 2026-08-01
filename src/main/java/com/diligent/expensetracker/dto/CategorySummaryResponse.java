package com.diligent.expensetracker.dto;

import com.diligent.expensetracker.enums.ExpenseCategory;

import java.math.BigDecimal;

public record CategorySummaryResponse(

        ExpenseCategory category,
        BigDecimal totalExpense,
        long totalTransactions

) {}