package com.example.ledger.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class Transaction {
    private final String id;
    private final String accountId;
    private final AccountType accountType;
    private final TransactionType type;
    private final BigDecimal amount;
    private final String currency;          // NEW
    private final OffsetDateTime timestamp;
    private final String referenceId;       // idempotency / client ref (optional)
    private final String transactionCode;   // NEW - e.g., "ATM-DEP-001" or business code

    public Transaction(String id,
                       String accountId,
                       AccountType accountType,
                       TransactionType type,
                       BigDecimal amount,
                       String currency,
                       OffsetDateTime timestamp,
                       String referenceId,
                       String transactionCode) {
        this.id = id;
        this.accountId = accountId;
        this.accountType = accountType;
        this.type = type;
        this.amount = amount;
        this.currency = currency;
        this.timestamp = timestamp;
        this.referenceId = referenceId;
        this.transactionCode = transactionCode;
    }

    public String getId() { return id; }
    public String getAccountId() { return accountId; }
    public AccountType getAccountType() { return accountType; }
    public TransactionType getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public OffsetDateTime getTimestamp() { return timestamp; }
    public String getReferenceId() { return referenceId; }
    public String getTransactionCode() { return transactionCode; }
}
