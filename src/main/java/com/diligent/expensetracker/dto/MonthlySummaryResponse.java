package com.diligent.expensetracker.dto;

import java.math.BigDecimal;

public record MonthlySummaryResponse(

        int year,
        int month,
        BigDecimal totalExpense,
        long totalTransactions

) {}