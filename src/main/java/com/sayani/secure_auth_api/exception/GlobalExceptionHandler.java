package com.sayani.secure_auth_api.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Handles CustomMessageException returning HTTP 200 OK with proper custom exception message.
     */
    @ExceptionHandler(CustomMessageException.class)
    public ResponseEntity<Map<String, Object>> handleCustomMessageException(CustomMessageException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", ex.getStatus() != null ? ex.getStatus() : "FAIL");
        body.put("message", ex.getMessage());
        body.put("success", false);
        body.put("timestamp", LocalDateTime.now().format(FORMATTER));
        return ResponseEntity.ok(body);
    }

    /**
     * Handles BadCredentialsException returning HTTP 200 OK with custom message.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(BadCredentialsException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "FAIL");
        body.put("message", "Invalid email or password");
        body.put("success", false);
        body.put("timestamp", LocalDateTime.now().format(FORMATTER));
        return ResponseEntity.ok(body);
    }

    /**
     * Handles IllegalArgumentException returning HTTP 200 OK with exception message.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "FAIL");
        body.put("message", ex.getMessage());
        body.put("success", false);
        body.put("timestamp", LocalDateTime.now().format(FORMATTER));
        return ResponseEntity.ok(body);
    }

    /**
     * Handles Bean Validation exceptions returning HTTP 200 OK with validation errors.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        String firstMessage = "Validation failed";
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
            firstMessage = error.getDefaultMessage();
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "FAIL");
        body.put("message", firstMessage);
        body.put("errors", fieldErrors);
        body.put("success", false);
        body.put("timestamp", LocalDateTime.now().format(FORMATTER));
        return ResponseEntity.ok(body);
    }

    /**
     * Handles all other unexpected exceptions returning HTTP 200 OK with exception details.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAllOtherExceptions(Exception ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "ERROR");
        body.put("message", ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred");
        body.put("success", false);
        body.put("timestamp", LocalDateTime.now().format(FORMATTER));
        return ResponseEntity.ok(body);
    }
}
