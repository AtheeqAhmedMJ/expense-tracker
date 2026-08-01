package com.diligent.expensetracker.dto;

import com.diligent.expensetracker.enums.ExpenseCategory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ExpenseResponse(

        Long id,
        String title,
        BigDecimal amount,
        ExpenseCategory category,
        LocalDate expenseDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {
}