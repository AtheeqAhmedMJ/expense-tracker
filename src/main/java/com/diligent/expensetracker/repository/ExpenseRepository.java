package com.diligent.expensetracker.repository;

import com.diligent.expensetracker.dto.CategorySummaryResponse;
import com.diligent.expensetracker.dto.MonthlySummaryResponse;
import com.diligent.expensetracker.entity.Expense;
import com.diligent.expensetracker.enums.ExpenseCategory;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class ExpenseRepository {

    private final ConcurrentMap<Long, Expense> expenses = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong();

    public List<Expense> findAll() {
        return new ArrayList<>(expenses.values());
    }

    public Optional<Expense> findById(Long id) {
        return Optional.ofNullable(expenses.get(id));
    }

    public Expense save(Expense expense) {
        if (expense == null) {
            throw new IllegalArgumentException("Expense must not be null");
        }

        LocalDateTime now = LocalDateTime.now();

        if (expense.getId() == null) {
            expense.setId(idGenerator.incrementAndGet());
            expense.setCreatedAt(now);
        }

        if (expense.getCreatedAt() == null) {
            expense.setCreatedAt(now);
        }

        expense.setUpdatedAt(now);
        expenses.put(expense.getId(), expense);

        return expense;
    }

    public void delete(Expense expense) {
        if (expense != null && expense.getId() != null) {
            expenses.remove(expense.getId());
        }
    }

    public List<Expense> findByCategory(ExpenseCategory category) {
        return expenses.values().stream()
                .filter(expense -> expense.getCategory() == category)
                .sorted(Comparator.comparing(Expense::getId))
                .collect(Collectors.toList());
    }

    public List<Expense> findByTitleContainingIgnoreCase(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return findAll();
        }

        String lower = keyword.toLowerCase();
        return expenses.values().stream()
                .filter(expense -> expense.getTitle() != null && expense.getTitle().toLowerCase().contains(lower))
                .sorted(Comparator.comparing(Expense::getId))
                .collect(Collectors.toList());
    }

    public BigDecimal getTotalExpense() {
        return expenses.values().stream()
                .map(Expense::getAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public long count() {
        return expenses.size();
    }

    public List<MonthlySummaryResponse> getMonthlySummary() {
        Map<Integer, Map<Integer, List<Expense>>> grouped = expenses.values().stream()
                .filter(expense -> expense.getExpenseDate() != null)
                .collect(Collectors.groupingBy(
                        expense -> expense.getExpenseDate().getYear(),
                        Collectors.groupingBy(expense -> expense.getExpenseDate().getMonthValue())
                ));

        return grouped.entrySet().stream()
                .flatMap(yearEntry -> yearEntry.getValue().entrySet().stream()
                        .map(monthEntry -> {
                            int year = yearEntry.getKey();
                            int month = monthEntry.getKey();
                            List<Expense> monthExpenses = monthEntry.getValue();
                            return new MonthlySummaryResponse(
                                    year,
                                    month,
                                    monthExpenses.stream()
                                            .map(Expense::getAmount)
                                            .filter(amount -> amount != null)
                                            .reduce(BigDecimal.ZERO, BigDecimal::add),
                                    monthExpenses.size()
                            );
                        }))
                .sorted(Comparator.comparing(MonthlySummaryResponse::year)
                        .thenComparing(MonthlySummaryResponse::month))
                .collect(Collectors.toList());
    }

    public List<CategorySummaryResponse> getCategorySummary() {
        Map<ExpenseCategory, List<Expense>> grouped = expenses.values().stream()
                .collect(Collectors.groupingBy(Expense::getCategory));

        return java.util.Arrays.stream(ExpenseCategory.values())
                .map(category -> {
                    List<Expense> categoryExpenses = grouped.getOrDefault(category, List.of());
                    BigDecimal total = categoryExpenses.stream()
                            .map(Expense::getAmount)
                            .filter(amount -> amount != null)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return new CategorySummaryResponse(category, total, categoryExpenses.size());
                })
                .sorted(Comparator.comparing(CategorySummaryResponse::category))
                .collect(Collectors.toList());
    }
}