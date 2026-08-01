package com.diligent.expensetracker.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum ExpenseCategory {

    FOOD,
    TRAVEL,
    HEALTH,
    SHOPPING,
    ENTERTAINMENT,
    OTHER;

    @JsonCreator
    public static ExpenseCategory from(String value) {
        return ExpenseCategory.valueOf(value.toUpperCase());
    }
}