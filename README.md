# Mini Transaction Settlement Service

A small, production-style transaction settlement backend built to practice event-driven, concurrency-safe processing patterns — inspired by real-world banking transaction migration and settlement work.

> 🚧 **Status: Phase 1 (MVP) complete.** Core account/transaction flow and async settlement via Kafka are working end to end. See [Roadmap](#roadmap) for what's next.

## Overview

This project simulates a simplified account/transaction settlement system: accounts hold a balance, transactions are submitted via a REST API, and settlement (balance validation + update) happens **asynchronously** through Apache Kafka rather than inline in the request — the same shape used by real payment/banking systems to keep APIs fast and decouple request acceptance from processing.

It's built to demonstrate:
- Async, event-driven processing with Kafka (producer/consumer within a single service)
- Concurrency-safe balance updates using pessimistic database locking
- Idempotent message handling (safe against duplicate Kafka delivery)
- Clean layered architecture (controller → service → repository) with centralized error handling

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

- **Java 21 / Spring Boot 4.1.1**
- **Spring Web** — REST API
- **Spring Data JPA** — persistence, pagination
- **Spring Kafka** — async producer/consumer
- **PostgreSQL** — via [Neon](https://neon.tech) (serverless Postgres)
- **Apache Kafka** — via [Aiven](https://aiven.io) (managed Kafka, free tier)
- **Bean Validation (Jakarta Validation)** — request validation
- **Lombok** — boilerplate reduction
- **RFC 7807 `ProblemDetail`** — structured, standardized error responses

## Features (Phase 1)

- Create and retrieve accounts, with paginated listing
- Submit a transaction (`DEBIT`/`CREDIT`) for async settlement
- Poll transaction status (`PENDING` → `PROCESSED` / `FAILED`)
- Concurrency-safe balance updates via pessimistic row locking, preventing overdrafts from simultaneous transactions on the same account
- Idempotent settlement — safe to process the same Kafka message twice without double-applying a balance change
- Centralized exception handling with consistent `ProblemDetail` JSON error responses

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
| `POST` | `/api/account` | Create a new account |
| `GET` | `/api/account/{id}` | Get an account by ID |
| `GET` | `/api/account?page=&size=&direction=` | List accounts (paginated) |
| `POST` | `/api/transaction` | Submit a transaction for async settlement (`202 Accepted`) |
| `GET` | `/api/transaction/{id}` | Get a transaction's current status |
| `GET` | `/api/transaction?page=&size=&direction=` | List transactions (paginated) |

## Getting Started

### Prerequisites
- JDK 21+
- Maven
- A PostgreSQL database (e.g. a free [Neon](https://neon.tech) instance)
- A Kafka broker (e.g. a free [Aiven](https://aiven.io) Kafka service)

### Configuration

Set the following as environment variables (or in `application.properties`):

```
spring.datasource.url=${JDBC_URL}
spring.datasource.username=${JDBC_USER}
spring.datasource.password=${JDBC_PASSWORD}

spring.kafka.bootstrap-servers=${KAFKA_URL}

spring.kafka.ssl.trust-store-location=${TRUSTORE_FILE}
spring.kafka.properties.ssl.truststore.password=${TRUSTORE_PASSWORD}

spring.kafka.ssl.key-store-location=${KEYSTORE_FILE}
spring.kafka.properties.ssl.keystore.password=${KEYSTORE_PASSWORD}
```

### Run

```bash
mvn spring-boot:run
```

### Example Requests

```bash
# Create an account
curl -X POST http://localhost:8080/api/account \
  -H "Content-Type: application/json" \
  -d '{"name": "Dwi Septihadi", "cif": "CIF00123"}'

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

## Roadmap

Small, incremental additions planned for future phases:

- [ ] Dead-letter topic and retry handling for failed message processing
- [ ] OpenAPI/Swagger documentation
- [ ] Unit tests for `TransactionService.settle()` (happy path, insufficient balance, duplicate-message idempotency)
- [ ] Filter transactions by account ID
- [ ] Basic logging/metrics for observability

## Author

**Dwi Septihadi**  
[Linkedin](https://linkedin.com/in/dwi5h) · [Github](https://github.com/dwi5h)
