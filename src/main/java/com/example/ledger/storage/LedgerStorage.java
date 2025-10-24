package com.example.ledger.storage;

import com.example.ledger.model.Transaction;

import java.util.List;
import java.util.Optional;

public interface LedgerStorage {

    /** Append transaction for an account */
    void appendTransaction(String accountId, Transaction tx);

    /** Check and add idempotency reference. Returns true if new, false if already exists */
    boolean checkAndAddIdempotency(String accountId, String referenceId);

    /** Get transaction by referenceId */
    Optional<Transaction> findByReference(String accountId, String referenceId);

    /** Get all transactions for an account */
    List<Transaction> getTransactionsForAccount(String accountId);

    /** Get all transactions across all accounts */
    List<Transaction> getAllTransactions();

    /** Get currency for an account */
    String getCurrency(String accountId);

    /** Set currency for an account */
    void setCurrency(String accountId, String currency);

    /** Check if account exists */
    boolean accountExists(String accountId);
}
