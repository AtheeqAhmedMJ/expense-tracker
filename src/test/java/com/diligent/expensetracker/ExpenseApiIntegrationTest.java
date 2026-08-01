package com.diligent.expensetracker;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-end smoke test through the real Spring context: real controller,
 * real service, real in-memory repository — nothing mocked.
 * <p>
 * This intentionally overlaps in scope with the unit and slice tests in
 * the {@code controller}, {@code service}, and {@code repository}
 * packages. Those isolate each layer's own logic and run fast; this one
 * exists to catch wiring problems (e.g. a bean that fails to autowire, a
 * mapper that isn't actually generated, serialization that behaves
 * differently end-to-end) that layer-isolated tests can't see. Keeping
 * this file to a single representative lifecycle, rather than
 * re-checking every branch, is what keeps the two suites complementary
 * instead of redundant.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ExpenseApiIntegrationTest {

    private static final String BASE_URL = "/api/v1/expenses";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void fullLifecycle_createReadUpdateDelete_worksEndToEnd() throws Exception {
        String createBody = """
                {
                  "title": "Coffee",
                  "amount": 150.00,
                  "category": "FOOD",
                  "expenseDate": "2026-08-01"
                }
                """;

        String createResponse = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").exists())
                .andReturn().getResponse().getContentAsString();

        long id = objectMapper.readTree(createResponse).get("data").get("id").asLong();

        mockMvc.perform(get(BASE_URL + "/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Coffee"));

        String updateBody = """
                {
                  "title": "Coffee (large)",
                  "amount": 220.00,
                  "category": "FOOD",
                  "expenseDate": "2026-08-01"
                }
                """;

        mockMvc.perform(put(BASE_URL + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Coffee (large)"));

        mockMvc.perform(delete(BASE_URL + "/{id}", id))
                .andExpect(status().isOk());

        mockMvc.perform(get(BASE_URL + "/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void summaryEndpoint_reflectsCreatedExpense() throws Exception {
        String createBody = """
                {
                  "title": "Integration summary check",
                  "amount": 75.00,
                  "category": "HEALTH",
                  "expenseDate": "2026-08-01"
                }
                """;

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated());

        mockMvc.perform(get(BASE_URL + "/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalExpense").exists())
                .andExpect(jsonPath("$.data.totalTransactions").exists());
    }
}
