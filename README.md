# Sainsbury's Product Aggregation & Pricing Service

## Overview

This project is a **production-ready Spring Boot microservice** that aggregates product catalogue data from external Sainsbury's Marketplace APIs, persists the enriched dataset in PostgreSQL, and exposes RESTful query APIs with pagination, filtering, and transactional integrity.

> **Note to candidates:** Please read all instructions carefully and run the tests as many times as necessary to validate your solution.

---

## Background

You are provided with two public endpoints that deliver JSON data about products available on the Sainsbury's Marketplace. A foundational Spring Boot application structure (`uk.co.sainsburys.interview.ProductsApplication`) has already been scaffolded for you. The necessary clients and models for fetching product details and prices from the external APIs have already been implemented.

Your task is to extend this application into a **fully tested, containerised microservice** that demonstrates senior-level backend engineering capability.

---

## External APIs

| Purpose | Endpoint |
|---|---|
| Product catalogue | `https://s3.eu-west-1.amazonaws.com/hackajob-assets1.p.hackajob/challenges/sainsbury_products/products_v2.json` |
| Pricing data | `https://s3.eu-west-1.amazonaws.com/hackajob-assets1.p.hackajob/challenges/sainsbury_products/products_price_v2.json` |

---

## Core Requirements

### 1. Data Ingestion & Persistence

- Fetch and merge product + pricing data from the two external APIs
- Persist the merged dataset into **PostgreSQL**
- Ensure idempotency — re-running ingestion must not produce duplicate records
- The entire ingestion pipeline must execute within a **single database transaction**; partial failures must be handled cleanly

### 2. REST API

You must implement the following endpoints in `src/main/java/uk/co/sainsburys/interview/controller/ProductController.java`, backed by a dedicated service class:

#### `POST /products/ingest`

Triggers the ingestion pipeline:

- Fetches data from both external APIs
- Merges product and pricing records using `product_uid` as the join key
- Persists merged records via `UPSERT` (`ON CONFLICT DO UPDATE`)
- Returns HTTP `200` on success

#### `GET /products`

Queries persisted products. Supports:

- **Pagination** — `page`, `size` query parameters
- **Filtering** — `product_type` query parameter
- **Sorting** — `unit_price`, `name` query parameters

Example response:

```json
[
  {
    "product_uid": "6447344",
    "product_type": "BASIC",
    "name": "Sainsbury's Skin on ASC Scottish Salmon Fillets x2 240g",
    "full_url": "https://www.sainsburys.co.uk/gol-ui/product/sainsburys-responsibly-sourced-scottish-salmon-fillet-x2-240g",
    "unit_price": 15.63
  }
]
```

#### `GET /products/{product_uid}`

Returns a single product by its unique identifier.

---

## Database Schema

```sql
CREATE TABLE products (
    product_uid   VARCHAR      PRIMARY KEY,
    name          TEXT         NOT NULL,
    product_type  VARCHAR      NOT NULL,
    full_url      TEXT,
    unit_price    NUMERIC(10,2),
    created_at    TIMESTAMP    DEFAULT NOW(),
    updated_at    TIMESTAMP    DEFAULT NOW()
);

CREATE INDEX idx_products_product_type ON products (product_type);
CREATE INDEX idx_products_unit_price   ON products (unit_price);
```

Database migrations must be managed using **Flyway** with versioned migration scripts (e.g., `V1__create_products_table.sql`).

---

## Project Structure

```
src/main/java/uk/co/sainsburys/interview/
├── controller/
│   └── ProductController.java
├── service/
│   └── ProductService.java
├── repository/
│   └── ProductRepository.java
├── client/
│   └── (external API clients — already provided)
├── model/
│   └── (domain models and DTOs)
└── ProductsApplication.java
```

---

## Java 17 / 21 Expectations

Use modern Java features intentionally throughout your implementation:

- **`record`** — for API request/response DTOs
- **`sealed interface`** — to model product type variants (optional, but a strong signal)
- **`switch expression`** — where conditional branching is required
- Clean null handling — avoid legacy null-check anti-patterns

---

## Edge Cases — Must Be Handled

| Scenario | Expected Behaviour |
|---|---|
| Product exists in catalogue but has no corresponding price | Persist with `unit_price` as `null`; do not discard the record |
| Duplicate product entries in external API response | Deduplicate before persisting; last-write wins is acceptable |
| External API call fails during ingestion | Propagate error; do not partially commit; return an appropriate HTTP error status |

---

## Docker & Infrastructure

### Minimum Requirement

Run PostgreSQL via Docker. Your application must connect to it via `application.properties` / `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/sainsburys
    username: <Provided by environment>
    password: <Provided by environment>
```

### Preferred (Strong Signal)

Provide a `docker-compose.yml` that brings up both the application and the database:

```yaml
services:
  postgres:
    image: postgres:15
    environment:
      POSTGRES_DB: sainsburys
      POSTGRES_USER: <Provisioned by Docker Compose>
      POSTGRES_PASSWORD: <Provisioned by Docker Compose>
    ports:
      - "5432:5432"

  app:
    build: .
    depends_on:
      - postgres
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/sainsburys
    ports:
      - "8080:8080"
```

---

## Testing Strategy

Testing is non-negotiable at senior level. The following test categories are required:

### Unit Tests

Cover the core service logic in isolation:

- Product-to-price merge logic
- Validation rules (e.g., missing price handling)
- Edge case behaviour

### Integration Tests — Testcontainers (Required)

Use [Testcontainers](https://testcontainers.com/) to spin up a real PostgreSQL instance during test execution. Demonstrate:

- PostgreSQL container starts and schema is applied via Flyway
- Full ingestion pipeline runs end-to-end
- Query endpoints return correctly paginated and filtered results

### Idempotency Test (Standout Signal)

Run the `POST /products/ingest` endpoint twice in sequence and assert that the total number of rows in the `products` table equals the number of unique products returned by the external API — not double.

---

## Observability

Add the following to demonstrate production awareness:

- **Structured logging** — log key lifecycle events: ingestion start, record count fetched, persistence complete, errors
- **Spring Boot Actuator** — expose `/actuator/health` and `/actuator/info`

---

## Optional Enhancements (Differentiators)

| Enhancement | Description |
|---|---|
| Response caching | Cache `GET /products` responses to reduce database load |
| Retry logic | Retry failed external API calls with exponential backoff |
| Parallel fetch | Fetch both external APIs concurrently using `CompletableFuture` |

---

## Evaluation Criteria

Your submission will be assessed across the following dimensions:

| Dimension | What We Look For |
|---|---|
| **Backend engineering depth** | Real system design beyond a basic merge-and-expose pattern |
| **Database correctness** | Schema design, indexing, idempotent upserts, transaction integrity |
| **Modern Java usage** | Records, sealed interfaces, switch expressions — used with intent |
| **Testing maturity** | Unit tests for logic; Testcontainers for integration; idempotency verification |
| **Production awareness** | Logging, Actuator, Docker, error handling |

---

## How to Position Your Solution

When presenting or explaining your implementation, do not describe it as:

> *"I merged two APIs and exposed an endpoint."*

Instead, articulate it as:

> *"I designed a microservice that ingests external catalogue data, ensures idempotent persistence using PostgreSQL upserts, and exposes query APIs with pagination, filtering, and transactional integrity. I validated the system using Testcontainers-based integration tests and containerised it with Docker Compose."*

---

## Getting Started

```bash
# Start infrastructure
docker-compose up -d postgres

# Run the application
./mvnw spring-boot:run

# Trigger ingestion
curl -X POST http://localhost:8080/products/ingest

# Query products
curl "http://localhost:8080/products?page=0&size=10&product_type=BASIC&sort=unit_price"
```