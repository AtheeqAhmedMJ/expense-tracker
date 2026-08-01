package com.diligent.expensetracker.controller;

import com.diligent.expensetracker.dto.CategorySummaryResponse;
import com.diligent.expensetracker.dto.ExpenseRequest;
import com.diligent.expensetracker.dto.ExpenseResponse;
import com.diligent.expensetracker.dto.ExpenseSummaryResponse;
import com.diligent.expensetracker.dto.MonthlySummaryResponse;
import com.diligent.expensetracker.enums.ExpenseCategory;
import com.diligent.expensetracker.exception.ExpenseNotFoundException;
import com.diligent.expensetracker.service.ExpenseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web-layer slice test for {@link ExpenseController}.
 * <p>
 * {@code @WebMvcTest} boots only the MVC infrastructure (controller,
 * {@code @RestControllerAdvice}, Jackson, bean validation) — no real
 * repository or business logic runs. The service is mocked so these
 * tests verify routing, status codes, request validation, and response
 * shape only. Service behavior itself is covered by
 * {@code ExpenseServiceImplTest}.
 */
@WebMvcTest(ExpenseController.class)
class ExpenseControllerTest {

    private static final String BASE_URL = "/api/v1/expenses";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ExpenseService expenseService;

    private ExpenseResponse sampleResponse() {
        return new ExpenseResponse(
                1L, "Coffee", new BigDecimal("150.00"),
                ExpenseCategory.FOOD, LocalDate.of(2026, 8, 1), null, null
        );
    }

    @Test
    void createExpense_validBody_returns201WithBody() throws Exception {
        when(expenseService.createExpense(any(ExpenseRequest.class))).thenReturn(sampleResponse());

        String requestBody = """
                {
                  "title": "Coffee",
                  "amount": 150.00,
                  "category": "FOOD",
                  "expenseDate": "2026-08-01"
                }
                """;

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("Coffee"))
                .andExpect(jsonPath("$.data.category").value("FOOD"));
    }

    @Test
    void createExpense_missingAmount_returns400_andNeverCallsService() throws Exception {
        String requestBody = """
                {
                  "title": "Incomplete expense",
                  "category": "FOOD",
                  "expenseDate": "2026-08-01"
                }
                """;

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        org.mockito.Mockito.verifyNoInteractions(expenseService);
    }

    @Test
    void createExpense_negativeAmount_returns400() throws Exception {
        String requestBody = """
                {
                  "title": "Refund?",
                  "amount": -5.00,
                  "category": "FOOD",
                  "expenseDate": "2026-08-01"
                }
                """;

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createExpense_malformedJson_returns400() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not-json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllExpenses_returns200WithList() throws Exception {
        when(expenseService.getAllExpenses()).thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].title").value("Coffee"));
    }

    @Test
    void getExpenseById_existingId_returns200() throws Exception {
        when(expenseService.getExpenseById(1L)).thenReturn(sampleResponse());

        mockMvc.perform(get(BASE_URL + "/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void getExpenseById_unknownId_returns404() throws Exception {
        when(expenseService.getExpenseById(999L)).thenThrow(new ExpenseNotFoundException(999L));

        mockMvc.perform(get(BASE_URL + "/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void getExpenseById_nonNumericId_returns400() throws Exception {
        mockMvc.perform(get(BASE_URL + "/{id}", "not-a-number"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateExpense_existingId_returns200WithUpdatedBody() throws Exception {
        ExpenseResponse updated = new ExpenseResponse(
                1L, "Updated title", new BigDecimal("200.00"),
                ExpenseCategory.SHOPPING, LocalDate.of(2026, 8, 2), null, null
        );
        when(expenseService.updateExpense(eq(1L), any(ExpenseRequest.class))).thenReturn(updated);

        String requestBody = """
                {
                  "title": "Updated title",
                  "amount": 200.00,
                  "category": "SHOPPING",
                  "expenseDate": "2026-08-02"
                }
                """;

        mockMvc.perform(put(BASE_URL + "/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Updated title"))
                .andExpect(jsonPath("$.data.category").value("SHOPPING"));
    }

    @Test
    void updateExpense_unknownId_returns404() throws Exception {
        when(expenseService.updateExpense(eq(999L), any(ExpenseRequest.class)))
                .thenThrow(new ExpenseNotFoundException(999L));

        String requestBody = """
                {
                  "title": "Doesn't matter",
                  "amount": 50.00,
                  "category": "OTHER",
                  "expenseDate": "2026-08-01"
                }
                """;

        mockMvc.perform(put(BASE_URL + "/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteExpense_existingId_returns200() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void deleteExpense_unknownId_returns404() throws Exception {
        org.mockito.Mockito.doThrow(new ExpenseNotFoundException(999L))
                .when(expenseService).deleteExpense(999L);

        mockMvc.perform(delete(BASE_URL + "/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getExpensesByCategory_returns200WithFilteredList() throws Exception {
        when(expenseService.getExpensesByCategory(ExpenseCategory.TRAVEL)).thenReturn(List.of());

        mockMvc.perform(get(BASE_URL + "/category/{category}", "TRAVEL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }
    @Test
void getCategorySummary_shouldReturnCategoryTotals() throws Exception {
 
    List<CategorySummaryResponse> summary = List.of(
            new CategorySummaryResponse(ExpenseCategory.FOOD, new BigDecimal("150.00"), 2),
            new CategorySummaryResponse(ExpenseCategory.TRAVEL, BigDecimal.ZERO, 0)
    );
 
    when(expenseService.getCategorySummary()).thenReturn(summary);
 
    mockMvc.perform(get("/api/v1/expenses/summary/by-category"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data", hasSize(2)))
            .andExpect(jsonPath("$.data[0].category").value("FOOD"))
            .andExpect(jsonPath("$.data[0].total").value(150.00))
            .andExpect(jsonPath("$.data[0].count").value(2));
}
    @Test
    void getExpensesByCategory_invalidCategory_returns400() throws Exception {
        mockMvc.perform(get(BASE_URL + "/category/{category}", "NOT_A_CATEGORY"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchExpenses_withKeyword_returns200() throws Exception {
        when(expenseService.searchExpenses("coffee")).thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get(BASE_URL + "/search").param("keyword", "coffee"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].title").value("Coffee"));
    }

    @Test
    void searchExpenses_missingKeywordParam_returns400() throws Exception {
        mockMvc.perform(get(BASE_URL + "/search"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getExpenseSummary_returns200WithTotals() throws Exception {
        when(expenseService.getExpenseSummary())
                .thenReturn(new ExpenseSummaryResponse(new BigDecimal("500.00"), 3L));

        mockMvc.perform(get(BASE_URL + "/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalExpense").value(500.00))
                .andExpect(jsonPath("$.data.totalTransactions").value(3));
    }

    @Test
    void getMonthlySummary_returns200WithList() throws Exception {
        when(expenseService.getMonthlySummary())
                .thenReturn(List.of(new MonthlySummaryResponse(2026, 8, new BigDecimal("100.00"), 2)));

        mockMvc.perform(get(BASE_URL + "/monthly-summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].year").value(2026))
                .andExpect(jsonPath("$.data[0].month").value(8));
    }

    @Test
    void getCategorySummary_returns200WithList() throws Exception {
        when(expenseService.getCategorySummary())
                .thenReturn(List.of(new CategorySummaryResponse(ExpenseCategory.FOOD, new BigDecimal("20.00"), 1)));

        mockMvc.perform(get(BASE_URL + "/summary/by-category"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].category").value("FOOD"));
    }
}
