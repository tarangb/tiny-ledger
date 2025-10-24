package com.example.ledger.dto;

import com.example.ledger.model.AccountType;
import com.example.ledger.model.TransactionType;

import java.math.BigDecimal;

public class TransactionRequest {
    private AccountType accountType;
    private TransactionType type;
    private BigDecimal amount;
    private String currency;       // NEW
    private String referenceId;    // optional idempotency key
    private String timestamp;      // optional
    private String transactionCode; // NEW

    public AccountType getAccountType() { return accountType; }
    public void setAccountType(AccountType accountType) { this.accountType = accountType; }

    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getReferenceId() { return referenceId; }
    public void setReferenceId(String referenceId) { this.referenceId = referenceId; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public String getTransactionCode() { return transactionCode; }
    public void setTransactionCode(String transactionCode) { this.transactionCode = transactionCode; }
}
