# 💰 Tiny Ledger Service

A simple Spring Boot–based ledger system that tracks account transactions, balances, and transaction history.

---

## 🚀 Overview

The **Tiny Ledger Service** is a lightweight REST API that simulates a small-scale ledger system.  
It allows you to:
- Perform credit and debit transactions.
- Retrieve ledger entries per account.
- Check account balances.

All data is stored **in memory**, making this ideal for demonstrations or POCs.

---

## ⚙️ Tech Stack

- **Java 22**
- **Spring Boot 3.2.5**
- **Maven**
- **REST APIs (JSON-based)**

---

## 🧩 Prerequisites

Make sure you have:
- **Java 22** installed and configured (`java -version`)
- **Maven** installed (`mvn -version`)
- **Postman** (recommended for API testing)

---

## 📥 Clone the Repository

```bash
git clone https://github.com/<your-username>/tiny-ledger.git
cd tiny-ledger
```

---
## 🏗️ Build the Application

```bash
mvn clean install
```

---

## ▶️ Run the Application
```bash
mvn spring-boot:run
```
Once started, the service will be available at:
👉 http://localhost:8080

---

## 🧠 Assumptions & Design Notes
- This is a lightweight, in-memory ledger prototype, meant to demonstrate transaction flow, not a production-grade accounting system.
- In-memory storage is used — all transactions are lost when the application stops.
- Each transaction represents a single-entry posting (only one record per debit or credit).
- In a real-world double-entry ledger, a debit in one account would automatically generate a corresponding credit in another.
- This implementation intentionally simplifies that by treating each operation independently.
- Currency consistency per account — once an account starts using a currency (e.g., USD), all subsequent transactions must use the same currency.
- Idempotency is handled — duplicate transaction requests with the same request ID will not be reprocessed.
- Balance validation —
- SAVINGS accounts cannot be overdrawn.
- CREDIT CARD accounts can go negative (representing debt).
- Designed for thread safety — concurrent deposits on the same account are synchronized per-account while allowing parallel updates on different accounts.

---

## 📮 API Usage
You can test all endpoints using the provided Postman collection file:
ledger-service.postman_collection.json

**Steps to use Postman:**
- 1 Open Postman.
- 2 Click Import → File.
- 3 Select ledger-service.postman_collection.json from this project.
- 4 Use the imported requests to call the API endpoints.

**API Endpoints**
- POST /api/accounts/{accountId}/transactions — create a transaction (deposit/withdrawal).
- GET /api/accounts/{accountId}/transactions — get account transaction history.
- GET /api/accounts/{accountId}/balance — get current balance.
- GET /api/accounts/{accountId}/balanceAt?at={ISO_TIMESTAMP} — get balance at timestamp.
- GET /api/ledger — get all ledger transactions (sorted by timestamp).

---

## 🧹 Clean Shutdown

To stop the server, press Ctrl + C in the terminal.

**Since data is stored in memory, all records are cleared on shutdown.**

