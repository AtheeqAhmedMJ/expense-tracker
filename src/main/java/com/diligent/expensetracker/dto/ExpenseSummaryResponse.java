package com.diligent.expensetracker.dto;

import java.math.BigDecimal;

public record ExpenseSummaryResponse(

        BigDecimal totalExpense,
        long totalTransactions

) {}