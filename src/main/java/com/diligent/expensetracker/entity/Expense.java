package com.diligent.expensetracker.entity;

import com.diligent.expensetracker.enums.ExpenseCategory;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Expense {

    private Long id;
    private String title;
    private BigDecimal amount;
    private ExpenseCategory category;
    private LocalDate expenseDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
