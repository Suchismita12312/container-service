package com.demo.web;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.http.*;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.support.WebExchangeBindException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class GlobalErrorHandlerTest {

    private final GlobalErrorHandler handler = new GlobalErrorHandler();

    @Test
    void testHandleWebFluxBind_WithFieldError() {
        WebExchangeBindException ex = mock(WebExchangeBindException.class);
        FieldError fe = new FieldError("request", "origin", "must not be blank");
        when(ex.getFieldErrors()).thenReturn(List.of(fe));

        ResponseEntity<Map<String, String>> response = handler.handleWebFluxBind(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(response.getBody()).containsEntry("error", "origin: must not be blank");
    }

    @Test
    void testHandleWebFluxBind_NoFieldErrors() {
        WebExchangeBindException ex = mock(WebExchangeBindException.class);
        when(ex.getFieldErrors()).thenReturn(Collections.emptyList());

        ResponseEntity<Map<String, String>> response = handler.handleWebFluxBind(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("error", "Validation error");
    }

    @Test
    void testHandleValidation_WithFieldError() {
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors())
                .thenReturn(List.of(new FieldError("booking", "destination", "must not be null")));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<Map<String, String>> response = handler.handleValidation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("error", "destination: must not be null");
    }

    @Test
    void testHandleValidation_NoFieldErrors() {
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(Collections.emptyList());
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<Map<String, String>> response = handler.handleValidation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("error", "Validation error");
    }


    @Test
    void testHandleConstraint_WithViolation() {
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> violation = mock(ConstraintViolation.class);
        Path mockPath = mock(Path.class);
        when(mockPath.toString()).thenReturn("quantity");
        when(violation.getPropertyPath()).thenReturn(mockPath);
        when(violation.getMessage()).thenReturn("must be greater than 0");

        ConstraintViolationException ex = new ConstraintViolationException(Set.of(violation));

        ResponseEntity<Map<String, String>> response = handler.handleConstraint(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("error", "quantity: must be greater than 0");

    }

    @Test
    void testHandleConstraint_NoViolations() {
        ConstraintViolationException ex = new ConstraintViolationException(Collections.emptySet());

        ResponseEntity<Map<String, String>> response = handler.handleConstraint(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("error", "Validation error");
    }


    @Test
    void testHandleData_ShouldReturnInternalServerError() {
        DataAccessException ex = mock(DataAccessException.class);

        ResponseEntity<Map<String, String>> response = handler.handleData(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(response.getBody())
                .containsEntry("message", "Sorry there was a problem processing your request");
    }


    @Test
    void testHandleOther_ShouldReturnInternalServerError() {
        Exception ex = new Exception("Unexpected failure");

        ResponseEntity<Map<String, String>> response = handler.handleOther(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(response.getBody())
                .containsEntry("message", "Sorry there was a problem processing your request");
    }
}
