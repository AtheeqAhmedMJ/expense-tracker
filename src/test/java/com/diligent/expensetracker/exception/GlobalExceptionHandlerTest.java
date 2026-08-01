package com.diligent.expensetracker.exception;

import com.diligent.expensetracker.response.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Direct unit tests for {@link GlobalExceptionHandler}. Each handler method
 * is called with a real or mocked exception instance and its
 * {@link ResponseEntity} is asserted — no MockMvc/Spring context needed,
 * since these are plain @ExceptionHandler methods.
 * <p>
 * The controller-level 400/404 paths in {@code ExpenseControllerTest}
 * exercise this same handler wired into a real MVC dispatch; these tests
 * cover the handler's own mapping logic in isolation, including branches
 * (like malformed-JSON and type-mismatch) that are easier to trigger
 * directly than through a full request.
 */
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleExpenseNotFound_returns404WithMessage() {
        ResponseEntity<ApiResponse<Void>> result =
                handler.handleExpenseNotFound(new ExpenseNotFoundException(42L));

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().success()).isFalse();
        assertThat(result.getBody().message()).contains("42");
    }

    @Test
    void handleValidationException_joinsFieldErrorMessages() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(
                new FieldError("expenseRequest", "amount", "Amount is required"),
                new FieldError("expenseRequest", "title", "Title is required")
        ));

        ResponseEntity<ApiResponse<Void>> result = handler.handleValidationException(ex);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(result.getBody().message())
                .contains("Amount is required")
                .contains("Title is required");
    }

    @Test
    void handleUnreadableBody_returns400() {
        HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);
        when(ex.getMostSpecificCause()).thenReturn(new RuntimeException("Unexpected token"));

        ResponseEntity<ApiResponse<Void>> result = handler.handleUnreadableBody(ex);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(result.getBody().message()).contains("Malformed request body");
    }

    @Test
    void handleTypeMismatch_includesValueAndParameterName() {
        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
        when(ex.getValue()).thenReturn("abc");
        when(ex.getName()).thenReturn("id");

        ResponseEntity<ApiResponse<Void>> result = handler.handleTypeMismatch(ex);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(result.getBody().message()).contains("abc").contains("id");
    }

    @Test
    void handleMissingParam_returns400() {
        MissingServletRequestParameterException ex =
                new MissingServletRequestParameterException("keyword", "String");

        ResponseEntity<ApiResponse<Void>> result = handler.handleMissingParam(ex);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(result.getBody().success()).isFalse();
    }

    @Test
    void handleGenericException_returns500() {
        ResponseEntity<ApiResponse<Void>> result =
                handler.handleGenericException(new RuntimeException("boom"));

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(result.getBody().message()).isEqualTo("boom");
    }
}
