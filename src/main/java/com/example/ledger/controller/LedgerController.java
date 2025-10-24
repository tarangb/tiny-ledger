package com.example.ledger.controller;

import com.example.ledger.dto.TransactionRequest;
import com.example.ledger.model.Transaction;
import com.example.ledger.service.LedgerService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class LedgerController {

    private final LedgerService ledgerService;

    // Inject interface instead of implementation
    public LedgerController(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    /**
     * POST /api/accounts/{accountId}/transactions
     * Body: { accountType, type, amount, referenceId (opt), timestamp (opt ISO) }
     */
    @PostMapping("/accounts/{accountId}/transactions")
    public ResponseEntity<Transaction> createTransaction(
            @PathVariable String accountId,
            @RequestBody TransactionRequest req) {

        OffsetDateTime ts = (req.getTimestamp() == null || req.getTimestamp().isBlank())
                ? null
                : OffsetDateTime.parse(req.getTimestamp());

        Transaction tx = ledgerService.recordTransaction(
                accountId,
                req.getAccountType(),
                req.getType(),
                req.getAmount(),
                ts,
                req.getReferenceId(),
                req.getTransactionCode(),
                req.getCurrency()
        );

        return ResponseEntity.ok(tx);
    }

    @GetMapping("/ledger")
    public ResponseEntity<List<Transaction>> getLedgerRows() {
        return ResponseEntity.ok(ledgerService.getLedgerRows());
    }

    @GetMapping("/accounts/{accountId}/transactions")
    public ResponseEntity<List<Transaction>> getAccountTransactions(@PathVariable String accountId) {
        return ResponseEntity.ok(ledgerService.getTransactionHistory(accountId));
    }

    @GetMapping("/accounts/{accountId}/balance")
    public ResponseEntity<Map<String, BigDecimal>> getCurrentBalance(@PathVariable String accountId) {
        return ResponseEntity.ok(Map.of("balance", ledgerService.getCurrentBalance(accountId)));
    }

    @GetMapping("/accounts/{accountId}/balanceAt")
    public ResponseEntity<Map<String, BigDecimal>> getBalanceAt(
            @PathVariable String accountId,
            @RequestParam("at") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime at) {
        return ResponseEntity.ok(Map.of("balance", ledgerService.getBalanceAt(accountId, at)));
    }
}
