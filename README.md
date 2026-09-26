# Mini Transaction Settlement Service

A small, production-style transaction settlement backend built to practice event-driven, concurrency-safe processing patterns — inspired by real-world banking transaction migration and settlement work.

> 🚧 **Status: Phase 1 (MVP) complete**, with validation hardening, pagination, Swagger docs, and initial unit tests added. See [Roadmap](#roadmap) for what's next.

## Overview

This project simulates a simplified account/transaction settlement system: accounts hold a balance, transactions are submitted via a REST API, and settlement (balance validation + update) happens **asynchronously** through Apache Kafka rather than inline in the request — the same shape used by real payment/banking systems to keep APIs fast and decouple request acceptance from processing.

It's built to demonstrate:
- Async, event-driven processing with Kafka (producer/consumer within a single service)
- Concurrency-safe balance updates using pessimistic database locking
- Idempotent message handling (safe against duplicate Kafka delivery)
- Clean layered architecture (controller → service → repository) with centralized, RFC 7807–compliant error handling
- Input validation that fails fast with proper `400` responses instead of leaking `500`s
- Unit-tested core business logic (settlement state transitions)

## Architecture

```
Client
  │  POST /api/transaction
  ▼
TransactionController  ──(Kafka producer)──▶  Kafka topic: txn-requested
  │ saves txn as PENDING                              │
  ▼                                                    ▼
PostgreSQL  ◀────────────────(reads/writes)──── TransactionWorker (Kafka consumer)
  ▲                                                    │
  │                                          settle(): lock account row,
  └──────────────────────────────────────────  validate balance, update,
                                                mark PROCESSED / FAILED
```

The controller and worker live in the **same Spring Boot application** — Kafka is used as an internal async pipeline between "accept the request" and "process it," not as a bridge between separate services.

## Tech Stack

- **Java 21 / Spring Boot**
- **Spring Web** — REST API
- **Spring Data JPA** — persistence, pagination, pessimistic locking
- **Spring Kafka** — async producer/consumer, SSL-secured connection
- **PostgreSQL** — via [Neon](https://neon.tech) (serverless Postgres)
- **Apache Kafka** — via [Aiven](https://aiven.io) (managed Kafka, free tier, SSL/keystore auth)
- **Bean Validation (Jakarta Validation)** — request validation, including pattern-based checks on IDs and enum-like fields
- **springdoc-openapi** — interactive Swagger UI (`/swagger-ui.html`)
- **Lombok** — boilerplate reduction
- **RFC 7807 `ProblemDetail`** — structured, standardized error responses
- **JUnit 5 + Mockito** — unit tests for core service logic

## Features

- Create and retrieve accounts, with paginated listing and optional initial balance
- Submit a transaction (`DEBIT`/`CREDIT`) for async settlement
- Poll transaction status (`PENDING` → `PROCESSED` / `FAILED`)
- Concurrency-safe balance updates via pessimistic row locking, preventing overdrafts from simultaneous transactions on the same account
- Idempotent settlement — safe to process the same Kafka message twice without double-applying a balance change
- Strict request validation (UUID format, enum values, positive amounts) returning clean `400` errors instead of server errors on bad input
- Centralized exception handling with consistent `ProblemDetail` JSON error responses
- Interactive API docs via Swagger UI
- Unit tests covering the core settlement state machine

## How It Works: The Async Settlement Flow

1. **`POST /api/transaction`** — the controller validates the request, saves the transaction as `PENDING`, and publishes the transaction ID to the `txn-requested` Kafka topic. Responds `202 Accepted` immediately — settlement hasn't happened yet.
2. **`TransactionWorker`** consumes the message via `@KafkaListener`.
3. **`TransactionService.settle()`**:
   - Skips processing if the transaction is no longer `PENDING` (idempotency guard against duplicate delivery)
   - Locks the account row (`PESSIMISTIC_WRITE`) so concurrent transactions on the same account can't race each other
   - Re-validates the balance against the *current* row (not a stale value) for `DEBIT` transactions
   - Applies the balance change and marks the transaction `PROCESSED`, or marks it `FAILED` if funds are insufficient
4. **`GET /api/transaction/{id}`** — client polls for the final status.

## API Reference

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/account` | Create a new account (optional `initialBalance`, defaults to 0) |
| `GET` | `/api/account/{id}` | Get an account by ID |
| `GET` | `/api/account?page=&size=&direction=` | List accounts (paginated) |
| `POST` | `/api/transaction` | Submit a transaction for async settlement (`202 Accepted`) |
| `GET` | `/api/transaction/{id}` | Get a transaction's current status |
| `GET` | `/api/transaction?page=&size=&direction=` | List transactions (paginated) |

Full interactive documentation is available at `/swagger-ui.html` once the app is running.

## Getting Started

### Prerequisites
- JDK 21
- Maven
- A PostgreSQL database (e.g. a free [Neon](https://neon.tech) instance)
- A Kafka broker with SSL auth (e.g. a free [Aiven](https://aiven.io) Kafka service), with your keystore/truststore files

### Configuration

Set the following as environment variables:

```
JDBC_URL=jdbc:postgresql://<your-neon-host>/<db>?sslmode=require
JDBC_USER=<your-db-username>
JDBC_PASSWORD=<your-db-password>

KAFKA_URL=<your-aiven-host>:<port>
TRUSTORE_FILE=file:/path/to/truststore.jks
TRUSTORE_PASSWORD=<your-truststore-password>
KEYSTORE_FILE=file:/path/to/keystore.p12
KEYSTORE_PASSWORD=<your-keystore-password>
```

> ⚠️ Never commit keystore/truststore files or real credentials. They're excluded via `.gitignore` — keep it that way.

### Run

```bash
mvn spring-boot:run
```

### Run Tests

```bash
mvn test
```

### Example Requests

```bash
# Create an account
curl -X POST http://localhost:8080/api/account \
  -H "Content-Type: application/json" \
  -d '{"name": "Dwi Septihadi", "cif": "CIF00123", "initialBalance": 100000}'

# Submit a transaction
curl -X POST http://localhost:8080/api/transaction \
  -H "Content-Type: application/json" \
  -d '{"accountId": "<account-uuid>", "amount": 50000, "type": "CREDIT"}'

# Check settlement status
curl http://localhost:8080/api/transaction/<transaction-uuid>
```

## Design Notes

- **Why `202 Accepted`, not `200 OK`?** The transaction isn't settled yet when the API responds — `202` honestly communicates "accepted for processing," matching real async settlement semantics.
- **Why re-check the balance in the worker instead of trusting the initial request?** Between submission and processing, other transactions on the same account may have already been queued. Re-validating against the current, row-locked balance right before applying the change is what actually prevents overdrafts under concurrent load — checking once at submission time would not be safe.
- **Why send only the transaction ID over Kafka, not the full payload?** Forces the worker to re-fetch current state from the database rather than potentially acting on stale data from the message.
- **Why regex-validate `accountId` and `type` in the request DTO?** Without it, an invalid UUID or an unrecognized transaction type would fail deep in the mapper/service layer with an unchecked exception, surfacing as a `500`. Validating at the boundary means bad input fails fast and clearly with a `400`, not a false alarm that looks like a server bug.

## Testing

Unit tests cover the core settlement logic in `TransactionService`:
- Sufficient balance → `PROCESSED`, balance correctly debited/credited
- Insufficient balance on `DEBIT` → `FAILED`, balance left untouched
- Idempotency — a transaction already settled is not reprocessed (protects against duplicate Kafka delivery)
- Not-found cases for both transaction and account
- `createPending()` correctly maps a valid request and correctly rejects an unknown account

Run with `mvn test`.

## Roadmap

Small, incremental additions planned for future phases:

- [ ] Dead-letter topic and retry handling for failed message processing
- [ ] Filter transactions by account ID
- [ ] Basic logging/metrics for observability
- [ ] Integration test with an embedded/test Kafka broker and database

## Author

**Dwi Septihadi**  
[Linkedin](https://linkedin.com/in/dwi5h) · [Github](https://github.com/dwi5h)
