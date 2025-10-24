package com.example.ledger.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler for Ledger API.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle illegal arguments (bad requests)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleBadArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    /**
     * Handle insufficient balance errors
     */
    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<String> handleInsufficientBalance(InsufficientBalanceException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    /**
     * Optional: handle any uncaught runtime exceptions
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntime(RuntimeException ex) {
        return ResponseEntity.internalServerError().body(ex.getMessage());
    }
}
