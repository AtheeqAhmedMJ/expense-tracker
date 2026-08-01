package com.diligent.expensetracker.repository;

import com.diligent.expensetracker.dto.CategorySummaryResponse;
import com.diligent.expensetracker.dto.MonthlySummaryResponse;
import com.diligent.expensetracker.entity.Expense;
import com.diligent.expensetracker.enums.ExpenseCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pure unit tests for {@link ExpenseRepository}.
 * <p>
 * No Spring context is spun up here — the repository is a plain
 * ConcurrentHashMap-backed class, so it's instantiated directly.
 * This keeps the test fast and isolates repository logic (id
 * generation, timestamps, filtering, aggregation) from the web
 * and service layers.
 */
class ExpenseRepositoryTest {

    private ExpenseRepository repository;

    @BeforeEach
    void setUp() {
        repository = new ExpenseRepository();
    }

    private Expense newExpense(String title, String amount, ExpenseCategory category, String date) {
        return Expense.builder()
                .title(title)
                .amount(new BigDecimal(amount))
                .category(category)
                .expenseDate(LocalDate.parse(date))
                .build();
    }

    @Test
    void save_newExpense_assignsIdAndTimestamps() {
        Expense expense = newExpense("Coffee", "150.00", ExpenseCategory.FOOD, "2026-08-01");

        Expense saved = repository.save(expense);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(saved.getCreatedAt()).isEqualTo(saved.getUpdatedAt());
    }

    @Test
    void save_existingExpense_preservesCreatedAtButBumpsUpdatedAt() throws InterruptedException {
        Expense saved = repository.save(newExpense("Coffee", "150.00", ExpenseCategory.FOOD, "2026-08-01"));
        var originalCreatedAt = saved.getCreatedAt();

        // Ensure the clock actually advances between saves.
        Thread.sleep(5);

        saved.setTitle("Coffee (updated)");
        Expense resaved = repository.save(saved);

        assertThat(resaved.getId()).isEqualTo(saved.getId());
        assertThat(resaved.getCreatedAt()).isEqualTo(originalCreatedAt);
        assertThat(resaved.getUpdatedAt()).isAfterOrEqualTo(originalCreatedAt);
    }

    @Test
    void save_assignsDistinctIncrementingIds() {
        Expense first = repository.save(newExpense("A", "1.00", ExpenseCategory.OTHER, "2026-08-01"));
        Expense second = repository.save(newExpense("B", "2.00", ExpenseCategory.OTHER, "2026-08-01"));

        assertThat(second.getId()).isGreaterThan(first.getId());
    }

    @Test
    void save_nullExpense_throwsIllegalArgumentException() {
        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> repository.save(null)
        );
    }

    @Test
    void findById_existingId_returnsExpense() {
        Expense saved = repository.save(newExpense("Coffee", "150.00", ExpenseCategory.FOOD, "2026-08-01"));

        Optional<Expense> found = repository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Coffee");
    }

    @Test
    void findById_unknownId_returnsEmpty() {
        assertThat(repository.findById(999_999L)).isEmpty();
    }

    @Test
    void findAll_returnsEverythingSaved() {
        repository.save(newExpense("A", "1.00", ExpenseCategory.OTHER, "2026-08-01"));
        repository.save(newExpense("B", "2.00", ExpenseCategory.OTHER, "2026-08-01"));

        assertThat(repository.findAll()).hasSize(2);
    }

    @Test
    void findAll_whenEmpty_returnsEmptyList() {
        assertThat(repository.findAll()).isEmpty();
    }

    @Test
    void delete_existingExpense_removesIt() {
        Expense saved = repository.save(newExpense("Coffee", "150.00", ExpenseCategory.FOOD, "2026-08-01"));

        repository.delete(saved);

        assertThat(repository.findById(saved.getId())).isEmpty();
    }

    @Test
    void delete_nullExpense_doesNotThrow() {
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> repository.delete(null));
    }

    @Test
    void findByCategory_returnsOnlyMatchingCategory_sortedById() {
        repository.save(newExpense("Bus", "40.00", ExpenseCategory.TRAVEL, "2026-08-01"));
        repository.save(newExpense("Pizza", "20.00", ExpenseCategory.FOOD, "2026-08-01"));
        repository.save(newExpense("Taxi", "60.00", ExpenseCategory.TRAVEL, "2026-08-01"));

        List<Expense> travel = repository.findByCategory(ExpenseCategory.TRAVEL);

        assertThat(travel).hasSize(2);
        assertThat(travel).extracting(Expense::getCategory)
                .containsOnly(ExpenseCategory.TRAVEL);
        assertThat(travel.get(0).getId()).isLessThan(travel.get(1).getId());
    }

    @Test
    void findByCategory_noMatches_returnsEmptyList() {
        repository.save(newExpense("Pizza", "20.00", ExpenseCategory.FOOD, "2026-08-01"));

        assertThat(repository.findByCategory(ExpenseCategory.HEALTH)).isEmpty();
    }

    @Test
    void findByTitleContainingIgnoreCase_matchesRegardlessOfCase() {
        repository.save(newExpense("Unique Search Target", "10.00", ExpenseCategory.OTHER, "2026-08-01"));
        repository.save(newExpense("Something else", "10.00", ExpenseCategory.OTHER, "2026-08-01"));

        List<Expense> found = repository.findByTitleContainingIgnoreCase("search target");

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getTitle()).isEqualTo("Unique Search Target");
    }

    @Test
    void findByTitleContainingIgnoreCase_blankKeyword_returnsAll() {
        repository.save(newExpense("A", "1.00", ExpenseCategory.OTHER, "2026-08-01"));
        repository.save(newExpense("B", "2.00", ExpenseCategory.OTHER, "2026-08-01"));

        assertThat(repository.findByTitleContainingIgnoreCase("   ")).hasSize(2);
        assertThat(repository.findByTitleContainingIgnoreCase(null)).hasSize(2);
    }

    @Test
    void getTotalExpense_sumsAllAmounts() {
        repository.save(newExpense("A", "10.50", ExpenseCategory.OTHER, "2026-08-01"));
        repository.save(newExpense("B", "5.25", ExpenseCategory.OTHER, "2026-08-01"));

        assertThat(repository.getTotalExpense()).isEqualByComparingTo("15.75");
    }

    @Test
    void getTotalExpense_whenEmpty_returnsZero() {
        assertThat(repository.getTotalExpense()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void count_returnsNumberOfStoredExpenses() {
        repository.save(newExpense("A", "1.00", ExpenseCategory.OTHER, "2026-08-01"));
        repository.save(newExpense("B", "2.00", ExpenseCategory.OTHER, "2026-08-01"));

        assertThat(repository.count()).isEqualTo(2);
    }

    @Test
    void getMonthlySummary_groupsByYearAndMonth() {
        repository.save(newExpense("Aug 1", "10.00", ExpenseCategory.OTHER, "2026-08-01"));
        repository.save(newExpense("Aug 2", "20.00", ExpenseCategory.OTHER, "2026-08-15"));
        repository.save(newExpense("Sep", "30.00", ExpenseCategory.OTHER, "2026-09-01"));

        List<MonthlySummaryResponse> summary = repository.getMonthlySummary();

        assertThat(summary).hasSize(2);

        MonthlySummaryResponse august = summary.stream()
                .filter(s -> s.year() == 2026 && s.month() == 8)
                .findFirst()
                .orElseThrow();
        assertThat(august.totalExpense()).isEqualByComparingTo("30.00");
        assertThat(august.totalTransactions()).isEqualTo(2);

        // Sorted by year then month.
        assertThat(summary.get(0).month()).isLessThanOrEqualTo(summary.get(1).month());
    }

    @Test
    void getCategorySummary_includesEveryCategoryEvenWithNoExpenses() {
        repository.save(newExpense("Pizza", "20.00", ExpenseCategory.FOOD, "2026-08-01"));

        List<CategorySummaryResponse> summary = repository.getCategorySummary();

        // Every enum value is represented, not just categories with data.
        assertThat(summary).hasSize(ExpenseCategory.values().length);

        CategorySummaryResponse food = summary.stream()
                .filter(s -> s.category() == ExpenseCategory.FOOD)
                .findFirst()
                .orElseThrow();
        assertThat(food.totalExpense()).isEqualByComparingTo("20.00");
        assertThat(food.totalTransactions()).isEqualTo(1);

        CategorySummaryResponse health = summary.stream()
                .filter(s -> s.category() == ExpenseCategory.HEALTH)
                .findFirst()
                .orElseThrow();
        assertThat(health.totalExpense()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(health.totalTransactions()).isEqualTo(0);
    }
}
