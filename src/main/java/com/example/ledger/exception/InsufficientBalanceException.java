package com.example.ledger.exception;

/**
 * Custom exception for insufficient funds.
 */
public class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException(String message) {
        super(message);
    }
}
