package com.diligent.expensetracker.service;

import com.diligent.expensetracker.dto.CategorySummaryResponse;
import com.diligent.expensetracker.dto.ExpenseRequest;
import com.diligent.expensetracker.dto.ExpenseResponse;
import com.diligent.expensetracker.dto.ExpenseSummaryResponse;
import com.diligent.expensetracker.dto.MonthlySummaryResponse;
import com.diligent.expensetracker.entity.Expense;
import com.diligent.expensetracker.enums.ExpenseCategory;
import com.diligent.expensetracker.exception.ExpenseNotFoundException;
import com.diligent.expensetracker.mapper.ExpenseMapper;
import com.diligent.expensetracker.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseMapper expenseMapper;

    private Expense findExpenseById(Long id) {

        log.debug("Finding expense with ID: {}", id);

        return expenseRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Expense with ID {} not found", id);
                    return new ExpenseNotFoundException(id);
                });
    }

    @Override
    public ExpenseResponse createExpense(ExpenseRequest request) {

        log.info("Creating expense: {}", request.title());

        Expense expense = expenseMapper.toEntity(request);

        Expense savedExpense = expenseRepository.save(expense);

        log.info("Expense created successfully with ID: {}", savedExpense.getId());

        return expenseMapper.toResponse(savedExpense);
    }

    @Override
    public List<ExpenseResponse> getAllExpenses() {

        log.info("Retrieving all expenses");

        List<ExpenseResponse> expenses = expenseRepository.findAll()
                .stream()
                .map(expenseMapper::toResponse)
                .toList();

        log.info("Retrieved {} expense(s)", expenses.size());

        return expenses;
    }

    @Override
    public ExpenseResponse getExpenseById(Long id) {

        log.info("Retrieving expense with ID: {}", id);

        Expense expense = findExpenseById(id);

        return expenseMapper.toResponse(expense);
    }

    @Override
    public ExpenseResponse updateExpense(Long id, ExpenseRequest request) {

        log.info("Updating expense with ID: {}", id);

        Expense expense = findExpenseById(id);

        expense.setTitle(request.title());
        expense.setAmount(request.amount());
        expense.setCategory(request.category());
        expense.setExpenseDate(request.expenseDate());

        Expense updatedExpense = expenseRepository.save(expense);

        log.info("Expense {} updated successfully", id);

        return expenseMapper.toResponse(updatedExpense);
    }

    @Override
    public void deleteExpense(Long id) {

        log.info("Deleting expense with ID: {}", id);

        Expense expense = findExpenseById(id);

        expenseRepository.delete(expense);

        log.info("Expense {} deleted successfully", id);
    }

    @Override
    public List<ExpenseResponse> getExpensesByCategory(ExpenseCategory category) {

        log.info("Filtering expenses by category: {}", category);

        return expenseRepository.findByCategory(category)
                .stream()
                .map(expenseMapper::toResponse)
                .toList();
    }

    @Override
    public List<ExpenseResponse> searchExpenses(String keyword) {

        log.info("Searching expenses using keyword: '{}'", keyword);

        return expenseRepository.findByTitleContainingIgnoreCase(keyword)
                .stream()
                .map(expenseMapper::toResponse)
                .toList();
    }

    @Override
    public ExpenseSummaryResponse getExpenseSummary() {

        log.info("Generating expense summary");

        return new ExpenseSummaryResponse(
                expenseRepository.getTotalExpense(),
                expenseRepository.count()
        );
    }

    @Override
    public List<MonthlySummaryResponse> getMonthlySummary() {

        log.info("Generating monthly expense summary");

        return expenseRepository.getMonthlySummary();
    }

    @Override
    public List<CategorySummaryResponse> getCategorySummary() {

        log.info("Generating category-wise expense summary");

        return expenseRepository.getCategorySummary();
    }
}