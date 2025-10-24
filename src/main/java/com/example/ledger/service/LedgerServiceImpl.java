package com.example.ledger.service;

import com.example.ledger.exception.InsufficientBalanceException;
import com.example.ledger.model.AccountType;
import com.example.ledger.model.Transaction;
import com.example.ledger.model.TransactionType;
import com.example.ledger.storage.LedgerStorage;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class LedgerServiceImpl implements LedgerService {

    private final LedgerStorage storage;

    public LedgerServiceImpl(LedgerStorage storage) {
        this.storage = storage;
    }

    @Override
    public Transaction recordTransaction(String accountId,
                                         AccountType accountType,
                                         TransactionType type,
                                         BigDecimal amount,
                                         OffsetDateTime timestamp,
                                         String referenceId,
                                         String transactionCode,
                                         String currency) {

        if (accountId == null || accountId.isBlank()) throw new IllegalArgumentException("accountId required");
        if (accountType == null) throw new IllegalArgumentException("accountType required");
        if (type == null) throw new IllegalArgumentException("transaction type required");
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("amount must be >= 0");
        if (currency == null || currency.isBlank()) throw new IllegalArgumentException("currency required");

        // Enforce per-account currency consistency
        String existingCurrency = storage.getCurrency(accountId);
        if (existingCurrency == null) {
            storage.setCurrency(accountId, currency);
        } else if (!existingCurrency.equalsIgnoreCase(currency)) {
            throw new IllegalArgumentException(String.format(
                    "Currency mismatch for account %s: expected '%s', got '%s'",
                    accountId, existingCurrency, currency
            ));
        }

        // Idempotency check
        if (!storage.checkAndAddIdempotency(accountId, referenceId)) {
            return storage.findByReference(accountId, referenceId)
                    .orElseThrow(() -> new IllegalStateException("Idempotent reference recorded but tx missing"));
        }

        OffsetDateTime ts = (timestamp == null) ? OffsetDateTime.now() : timestamp;

        // Compute current balance for SAVINGS withdrawal rules
        BigDecimal currentBalance = getCurrentBalance(accountId);
        if (accountType == AccountType.SAVINGS &&
                type == TransactionType.WITHDRAWAL &&
                currentBalance.compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Insufficient funds for withdrawal");
        }

        Transaction tx = new Transaction(UUID.randomUUID().toString(),
                accountId, accountType, type, amount, currency.toUpperCase(Locale.ROOT),
                ts, referenceId, transactionCode);

        // Append transaction
        storage.appendTransaction(accountId, tx);

        return tx;
    }

    @Override
    public BigDecimal getCurrentBalance(String accountId) {
        return getBalanceAt(accountId, OffsetDateTime.MAX);
    }

    @Override
    public BigDecimal getBalanceAt(String accountId, OffsetDateTime at) {
        List<Transaction> transactions = storage.getTransactionsForAccount(accountId);
        return transactions.stream()
                .filter(t -> !t.getTimestamp().isAfter(at))
                .map(this::signedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public List<Transaction> getTransactionHistory(String accountId) {
        List<Transaction> transactions = storage.getTransactionsForAccount(accountId);
        return transactions.stream()
                .sorted(Comparator.comparing(Transaction::getTimestamp))
                .collect(Collectors.toList());
    }

    @Override
    public List<Transaction> getLedgerRows() {
        List<Transaction> allTxs = storage.getAllTransactions();
        allTxs.sort(Comparator.comparing(Transaction::getTimestamp));
        return allTxs;
    }

    private BigDecimal signedAmount(Transaction t) {
        return t.getType() == TransactionType.DEPOSIT ? t.getAmount() : t.getAmount().negate();
    }
}
