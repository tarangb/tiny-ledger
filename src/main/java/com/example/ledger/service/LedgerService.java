package com.example.ledger.service;

import com.example.ledger.model.AccountType;
import com.example.ledger.model.Transaction;
import com.example.ledger.model.TransactionType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public interface LedgerService {

    Transaction recordTransaction(String accountId,
                                  AccountType accountType,
                                  TransactionType type,
                                  BigDecimal amount,
                                  OffsetDateTime timestamp,
                                  String referenceId,
                                  String transactionCode,
                                  String currency);

    BigDecimal getCurrentBalance(String accountId);

    BigDecimal getBalanceAt(String accountId, OffsetDateTime at);

    List<Transaction> getTransactionHistory(String accountId);

    List<Transaction> getLedgerRows();
}
