package com.diligent.expensetracker.mapper;

import com.diligent.expensetracker.dto.ExpenseRequest;
import com.diligent.expensetracker.dto.ExpenseResponse;
import com.diligent.expensetracker.entity.Expense;
import com.diligent.expensetracker.enums.ExpenseCategory;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the MapStruct-generated {@code ExpenseMapperImpl} through the
 * {@link ExpenseMapper} interface. Mappers.getMapper() is used instead of
 * autowiring, so this stays a plain unit test with no Spring context.
 */
class ExpenseMapperTest {

    private final ExpenseMapper mapper = Mappers.getMapper(ExpenseMapper.class);

    @Test
    void toEntity_mapsRequestFields_andIgnoresIdAndTimestamps() {
        ExpenseRequest request = new ExpenseRequest(
                "Coffee",
                new BigDecimal("150.00"),
                ExpenseCategory.FOOD,
                LocalDate.of(2026, 8, 1)
        );

        Expense entity = mapper.toEntity(request);

        assertThat(entity.getTitle()).isEqualTo("Coffee");
        assertThat(entity.getAmount()).isEqualByComparingTo("150.00");
        assertThat(entity.getCategory()).isEqualTo(ExpenseCategory.FOOD);
        assertThat(entity.getExpenseDate()).isEqualTo(LocalDate.of(2026, 8, 1));

        // These are deliberately left for the repository to assign on save.
        assertThat(entity.getId()).isNull();
        assertThat(entity.getCreatedAt()).isNull();
        assertThat(entity.getUpdatedAt()).isNull();
    }

    @Test
    void toResponse_mapsEveryField() {
        LocalDateTime now = LocalDateTime.of(2026, 8, 1, 12, 0);
        Expense entity = Expense.builder()
                .id(1L)
                .title("Coffee")
                .amount(new BigDecimal("150.00"))
                .category(ExpenseCategory.FOOD)
                .expenseDate(LocalDate.of(2026, 8, 1))
                .createdAt(now)
                .updatedAt(now)
                .build();

        ExpenseResponse response = mapper.toResponse(entity);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.title()).isEqualTo("Coffee");
        assertThat(response.amount()).isEqualByComparingTo("150.00");
        assertThat(response.category()).isEqualTo(ExpenseCategory.FOOD);
        assertThat(response.expenseDate()).isEqualTo(LocalDate.of(2026, 8, 1));
        assertThat(response.createdAt()).isEqualTo(now);
        assertThat(response.updatedAt()).isEqualTo(now);
    }

    @Test
    void toResponse_nullInput_returnsNull() {
        assertThat(mapper.toResponse(null)).isNull();
    }

    @Test
    void toEntity_nullInput_returnsNull() {
        assertThat(mapper.toEntity(null)).isNull();
    }
}
