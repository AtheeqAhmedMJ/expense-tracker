package com.diligent.expensetracker.service;

import com.diligent.expensetracker.dto.CategorySummaryResponse;
import com.diligent.expensetracker.dto.ExpenseRequest;
import com.diligent.expensetracker.dto.ExpenseResponse;
import com.diligent.expensetracker.dto.MonthlySummaryResponse;
import com.diligent.expensetracker.entity.Expense;
import com.diligent.expensetracker.enums.ExpenseCategory;
import com.diligent.expensetracker.exception.ExpenseNotFoundException;
import com.diligent.expensetracker.mapper.ExpenseMapper;
import com.diligent.expensetracker.repository.ExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ExpenseServiceImpl}. The repository and mapper are
 * mocked so this test is isolated to the service's own orchestration logic
 * (e.g. "does it throw ExpenseNotFoundException when the repo returns
 * empty?", "does update mutate the right fields before saving?").
 */
@ExtendWith(MockitoExtension.class)
class ExpenseServiceImplTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private ExpenseMapper expenseMapper;

    @InjectMocks
    private ExpenseServiceImpl expenseService;

    private Expense entity;
    private ExpenseResponse response;
    private ExpenseRequest request;

    @BeforeEach
    void setUp() {
        entity = Expense.builder()
                .id(1L)
                .title("Coffee")
                .amount(new BigDecimal("150.00"))
                .category(ExpenseCategory.FOOD)
                .expenseDate(LocalDate.of(2026, 8, 1))
                .build();

        response = new ExpenseResponse(
                1L, "Coffee", new BigDecimal("150.00"),
                ExpenseCategory.FOOD, LocalDate.of(2026, 8, 1), null, null
        );

        request = new ExpenseRequest(
                "Coffee", new BigDecimal("150.00"), ExpenseCategory.FOOD, LocalDate.of(2026, 8, 1)
        );
    }

    @Test
    void createExpense_mapsSavesAndReturnsResponse() {
        when(expenseMapper.toEntity(request)).thenReturn(entity);
        when(expenseRepository.save(entity)).thenReturn(entity);
        when(expenseMapper.toResponse(entity)).thenReturn(response);

        ExpenseResponse result = expenseService.createExpense(request);

        assertThat(result).isEqualTo(response);
        verify(expenseRepository).save(entity);
    }

    @Test
    void getAllExpenses_mapsEveryRepositoryEntry() {
        when(expenseRepository.findAll()).thenReturn(List.of(entity));
        when(expenseMapper.toResponse(entity)).thenReturn(response);

        List<ExpenseResponse> result = expenseService.getAllExpenses();

        assertThat(result).containsExactly(response);
    }

    @Test
    void getAllExpenses_whenNoneExist_returnsEmptyList() {
        when(expenseRepository.findAll()).thenReturn(List.of());

        assertThat(expenseService.getAllExpenses()).isEmpty();
        verifyNoInteractions(expenseMapper);
    }

    @Test
    void getExpenseById_existingId_returnsMappedResponse() {
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(expenseMapper.toResponse(entity)).thenReturn(response);

        assertThat(expenseService.getExpenseById(1L)).isEqualTo(response);
    }

    @Test
    void getExpenseById_unknownId_throwsExpenseNotFoundException() {
        when(expenseRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.getExpenseById(999L))
                .isInstanceOf(ExpenseNotFoundException.class);

        verifyNoInteractions(expenseMapper);
    }

    @Test
    void updateExpense_existingId_mutatesFieldsAndSaves() {
        ExpenseRequest updateRequest = new ExpenseRequest(
                "Updated title", new BigDecimal("200.00"), ExpenseCategory.SHOPPING, LocalDate.of(2026, 8, 2)
        );

        when(expenseRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(expenseRepository.save(any(Expense.class))).thenAnswer(inv -> inv.getArgument(0));
        when(expenseMapper.toResponse(any(Expense.class))).thenReturn(response);

        expenseService.updateExpense(1L, updateRequest);

        ArgumentCaptor<Expense> captor = ArgumentCaptor.forClass(Expense.class);
        verify(expenseRepository).save(captor.capture());

        Expense saved = captor.getValue();
        assertThat(saved.getTitle()).isEqualTo("Updated title");
        assertThat(saved.getAmount()).isEqualByComparingTo("200.00");
        assertThat(saved.getCategory()).isEqualTo(ExpenseCategory.SHOPPING);
        assertThat(saved.getExpenseDate()).isEqualTo(LocalDate.of(2026, 8, 2));
    }

    @Test
    void updateExpense_unknownId_throwsExpenseNotFoundException_andNeverSaves() {
        when(expenseRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.updateExpense(999L, request))
                .isInstanceOf(ExpenseNotFoundException.class);

        verify(expenseRepository, never()).save(any());
    }

    @Test
    void deleteExpense_existingId_delegatesToRepository() {
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(entity));

        expenseService.deleteExpense(1L);

        verify(expenseRepository).delete(entity);
    }

    @Test
    void deleteExpense_unknownId_throwsExpenseNotFoundException_andNeverDeletes() {
        when(expenseRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.deleteExpense(999L))
                .isInstanceOf(ExpenseNotFoundException.class);

        verify(expenseRepository, never()).delete(any());
    }

    @Test
    void getExpensesByCategory_delegatesToRepositoryAndMapsResults() {
        when(expenseRepository.findByCategory(ExpenseCategory.FOOD)).thenReturn(List.of(entity));
        when(expenseMapper.toResponse(entity)).thenReturn(response);

        assertThat(expenseService.getExpensesByCategory(ExpenseCategory.FOOD)).containsExactly(response);
    }

    @Test
    void searchExpenses_delegatesToRepositoryAndMapsResults() {
        when(expenseRepository.findByTitleContainingIgnoreCase("coffee")).thenReturn(List.of(entity));
        when(expenseMapper.toResponse(entity)).thenReturn(response);

        assertThat(expenseService.searchExpenses("coffee")).containsExactly(response);
    }

    @Test
    void getExpenseSummary_combinesTotalAndCountFromRepository() {
        when(expenseRepository.getTotalExpense()).thenReturn(new BigDecimal("500.00"));
        when(expenseRepository.count()).thenReturn(3L);

        var summary = expenseService.getExpenseSummary();

        assertThat(summary.totalExpense()).isEqualByComparingTo("500.00");
        assertThat(summary.totalTransactions()).isEqualTo(3L);
    }

    @Test
    void getMonthlySummary_returnsWhateverRepositoryProvides() {
        List<MonthlySummaryResponse> expected = List.of(
                new MonthlySummaryResponse(2026, 8, new BigDecimal("100.00"), 2)
        );
        when(expenseRepository.getMonthlySummary()).thenReturn(expected);

        assertThat(expenseService.getMonthlySummary()).isEqualTo(expected);
    }

    @Test
    void getCategorySummary_returnsWhateverRepositoryProvides() {
        List<CategorySummaryResponse> expected = List.of(
                new CategorySummaryResponse(ExpenseCategory.FOOD, new BigDecimal("20.00"), 1)
        );
        when(expenseRepository.getCategorySummary()).thenReturn(expected);

        assertThat(expenseService.getCategorySummary()).isEqualTo(expected);
    }
}
