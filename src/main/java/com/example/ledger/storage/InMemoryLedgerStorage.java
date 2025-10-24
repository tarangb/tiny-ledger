package com.example.ledger.storage;

import com.example.ledger.model.Transaction;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

/**
 * In-memory implementation of LedgerStorage
 */
@Component
public class InMemoryLedgerStorage implements LedgerStorage {

    private final ConcurrentHashMap<String, ConcurrentLinkedQueue<Transaction>> transactionsByAccount;
    private final ConcurrentHashMap<String, Set<String>> idempotencyMap;
    private final ConcurrentHashMap<String, String> accountCurrencyMap;

    public InMemoryLedgerStorage() {
        this.transactionsByAccount = new ConcurrentHashMap<>();
        this.idempotencyMap = new ConcurrentHashMap<>();
        this.accountCurrencyMap = new ConcurrentHashMap<>();
    }

    @Override
    public void appendTransaction(String accountId, Transaction tx) {
        transactionsByAccount
                .computeIfAbsent(accountId, k -> new ConcurrentLinkedQueue<>())
                .add(tx);
    }

    @Override
    public boolean checkAndAddIdempotency(String accountId, String referenceId) {
        if (referenceId == null || referenceId.isBlank()) return true;

        idempotencyMap.putIfAbsent(accountId, ConcurrentHashMap.newKeySet());
        return idempotencyMap.get(accountId).add(referenceId);
    }

    @Override
    public Optional<Transaction> findByReference(String accountId, String referenceId) {
        ConcurrentLinkedQueue<Transaction> queue = transactionsByAccount.get(accountId);
        if (queue == null) return Optional.empty();
        return queue.stream()
                .filter(tx -> referenceId.equals(tx.getReferenceId()))
                .findFirst();
    }

    @Override
    public List<Transaction> getTransactionsForAccount(String accountId) {
        ConcurrentLinkedQueue<Transaction> queue = transactionsByAccount.get(accountId);
        if (queue == null) return List.of();
        return queue.stream().collect(Collectors.toList());
    }

    @Override
    public List<Transaction> getAllTransactions() {
        return transactionsByAccount.values().stream()
                .flatMap(ConcurrentLinkedQueue::stream)
                .collect(Collectors.toList());
    }

    @Override
    public String getCurrency(String accountId) {
        return accountCurrencyMap.get(accountId);
    }

    @Override
    public void setCurrency(String accountId, String currency) {
        accountCurrencyMap.putIfAbsent(accountId, currency.toUpperCase());
    }

    @Override
    public boolean accountExists(String accountId) {
        return transactionsByAccount.containsKey(accountId);
    }
}
