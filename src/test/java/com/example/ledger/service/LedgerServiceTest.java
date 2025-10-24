package com.example.ledger.service;

import com.example.ledger.exception.InsufficientBalanceException;
import com.example.ledger.model.AccountType;
import com.example.ledger.model.TransactionType;
import com.example.ledger.model.Transaction;
import com.example.ledger.storage.InMemoryLedgerStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for LedgerService using InMemoryLedgerStorage.
 * Covers deposit, withdrawal, idempotency, currency checks,
 * ledger retrieval, and concurrency.
 */
class LedgerServiceTest {

    private LedgerService service;

    @BeforeEach
    void setUp() {
        service = new LedgerServiceImpl(new InMemoryLedgerStorage());
    }

    @Test
    void depositStoresCurrencyAndCode() {
        Transaction tx = service.recordTransaction("A1", AccountType.SAVINGS, TransactionType.DEPOSIT,
                new BigDecimal("100.00"), null, null, "ATM-DEP-001", "USD");

        assertEquals("USD", tx.getCurrency());
        assertEquals("ATM-DEP-001", tx.getTransactionCode());

        List<Transaction> hist = service.getTransactionHistory("A1");
        assertEquals(1, hist.size());
        assertEquals("USD", hist.get(0).getCurrency());
    }

    @Test
    void savingsOverdrawThrows() {
        service.recordTransaction("A1", AccountType.SAVINGS, TransactionType.DEPOSIT,
                new BigDecimal("50.00"), null, null, null, "USD");

        assertThrows(InsufficientBalanceException.class, () ->
                service.recordTransaction("A1", AccountType.SAVINGS, TransactionType.WITHDRAWAL,
                        new BigDecimal("100.00"), null, null, null, "USD"));
    }

    @Test
    void idempotencyPreventsDuplicateTransactions() {
        String requestId = "req-xyz";

        Transaction t1 = service.recordTransaction("A1", AccountType.SAVINGS, TransactionType.DEPOSIT,
                new BigDecimal("200.00"), null, requestId, "CODE-1", "USD");

        Transaction t2 = service.recordTransaction("A1", AccountType.SAVINGS, TransactionType.DEPOSIT,
                new BigDecimal("200.00"), null, requestId, "CODE-1", "USD");

        assertEquals(t1.getId(), t2.getId(), "Duplicate request should return same transaction");
        List<Transaction> hist = service.getTransactionHistory("A1");
        assertEquals(1, hist.size());
    }

    @Test
    void ledgerReceivesAppends() {
        service.recordTransaction("B1", AccountType.SAVINGS, TransactionType.DEPOSIT,
                new BigDecimal("100.00"), OffsetDateTime.parse("2025-10-01T10:00:00Z"),
                null, "CODE-A", "USD");

        List<Transaction> ledgerRows = service.getTransactionHistory("B1");
        assertEquals(1, ledgerRows.size());
        assertEquals("CODE-A", ledgerRows.get(0).getTransactionCode());
    }

    @Test
    void currencyMismatchThrows() {
        service.recordTransaction("C1", AccountType.SAVINGS, TransactionType.DEPOSIT,
                new BigDecimal("100.00"), null, null, "CODE-USD", "USD");

        assertThrows(IllegalArgumentException.class, () ->
                service.recordTransaction("C1", AccountType.SAVINGS, TransactionType.WITHDRAWAL,
                        new BigDecimal("10.00"), null, null, "CODE-INR", "INR"));
    }

    @Test
    void supportsMultipleCurrenciesAcrossAccounts() {
        service.recordTransaction("D1", AccountType.SAVINGS, TransactionType.DEPOSIT,
                new BigDecimal("100.00"), null, null, "CODE-USD", "USD");

        service.recordTransaction("D2", AccountType.SAVINGS, TransactionType.DEPOSIT,
                new BigDecimal("500.00"), null, null, "CODE-INR", "INR");

        assertEquals("USD", service.getTransactionHistory("D1").get(0).getCurrency());
        assertEquals("INR", service.getTransactionHistory("D2").get(0).getCurrency());
    }

    @Test
    void accountLevelLockingAllowsParallelDifferentAccounts() throws InterruptedException, ExecutionException {
        ExecutorService exec = Executors.newFixedThreadPool(4);

        Callable<Void> taskA = () -> {
            for (int i = 0; i < 50; i++) {
                service.recordTransaction("A1", AccountType.SAVINGS, TransactionType.DEPOSIT,
                        new BigDecimal("1.00"), null, null, null, "USD");
            }
            return null;
        };

        Callable<Void> taskB = () -> {
            for (int i = 0; i < 50; i++) {
                service.recordTransaction("A2", AccountType.SAVINGS, TransactionType.DEPOSIT,
                        new BigDecimal("1.00"), null, null, null, "USD");
            }
            return null;
        };

        Future<Void> f1 = exec.submit(taskA);
        Future<Void> f2 = exec.submit(taskB);
        f1.get();
        f2.get();
        exec.shutdown();

        assertEquals(new BigDecimal("50.00"), service.getCurrentBalance("A1"));
        assertEquals(new BigDecimal("50.00"), service.getCurrentBalance("A2"));
    }

    @Test
    void concurrentDepositsOnSameAccountAreThreadSafe() throws InterruptedException {
        int threads = 10;
        int iterations = 20;
        ExecutorService exec = Executors.newFixedThreadPool(threads);

        CountDownLatch latch = new CountDownLatch(threads);
        for (int i = 0; i < threads; i++) {
            exec.submit(() -> {
                for (int j = 0; j < iterations; j++) {
                    service.recordTransaction("E1", AccountType.SAVINGS, TransactionType.DEPOSIT,
                            new BigDecimal("1.00"), null, null, null, "USD");
                }
                latch.countDown();
            });
        }
        latch.await();
        exec.shutdown();

        BigDecimal expected = BigDecimal.valueOf(threads * iterations);
        BigDecimal actual = service.getCurrentBalance("E1");

        assertEquals(0, expected.compareTo(actual), "Expected balance " + expected + " but got " + actual);
    }

    // ---------------- CREDIT CARD TESTS ----------------

    @Test
    void creditCardWithdrawalIncreasesDebt() {
        service.recordTransaction("CC1", AccountType.CREDIT_CARD, TransactionType.WITHDRAWAL,
                new BigDecimal("100.00"), null, null, "SPEND-1", "USD");

        BigDecimal balance = service.getCurrentBalance("CC1");
        assertEquals(0, balance.compareTo(new BigDecimal("-100.00")));
    }

    @Test
    void creditCardDepositReducesDebt() {
        service.recordTransaction("CC2", AccountType.CREDIT_CARD, TransactionType.WITHDRAWAL,
                new BigDecimal("200.00"), null, null, "SPEND-1", "USD");
        service.recordTransaction("CC2", AccountType.CREDIT_CARD, TransactionType.DEPOSIT,
                new BigDecimal("50.00"), null, null, "PAYMENT-1", "USD");

        BigDecimal balance = service.getCurrentBalance("CC2");
        assertEquals(0, balance.compareTo(new BigDecimal("-150.00")));
    }

    @Test
    void creditCardDoesNotThrowOnOverdraw() {
        assertDoesNotThrow(() ->
                service.recordTransaction("CC2", AccountType.CREDIT_CARD, TransactionType.WITHDRAWAL,
                        new BigDecimal("9999.99"), null, null, "SPEND-TEST", "USD"));
    }

    @Test
    void creditCardCurrencyMismatchThrows() {
        service.recordTransaction("CC3", AccountType.CREDIT_CARD, TransactionType.DEPOSIT,
                new BigDecimal("100.00"), null, null, "PAYMENT-USD", "USD");
        assertThrows(IllegalArgumentException.class, () ->
                service.recordTransaction("CC3", AccountType.CREDIT_CARD, TransactionType.WITHDRAWAL,
                        new BigDecimal("50.00"), null, null, "SPEND-INR", "INR"));
    }
}
