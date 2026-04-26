# Requirements

## Overview
A Spring Boot microservice that fetches product and pricing data from two external JSON APIs, merges them by `product_uid`, and upserts the result into a PostgreSQL database via a single on-demand REST endpoint.

## User Stories

### US-1: Trigger product ingestion
As an operator, I want to call `POST /products/ingest` so that the database is populated with the latest product and pricing data.

**Acceptance Criteria**
- AC-1.1: A `POST /products/ingest` request triggers a full fetch → merge → upsert cycle
- AC-1.2: The endpoint returns `202 Accepted` when ingestion completes successfully
- AC-1.3: The endpoint is idempotent — repeated calls produce the same final database state

### US-2: Merge product and price data
As the system, I want to join product records with their prices by `product_uid` so that each persisted product carries its unit price.

**Acceptance Criteria**
- AC-2.1: Each product is matched to its price using `product_uid` as the join key
- AC-2.2: Products with no matching price entry are persisted with `unitPrice = null`
- AC-2.3: When a `product_uid` appears multiple times in the price feed, the last entry wins (last-write-wins)

### US-3: Persist products idempotently
As the system, I want upserts to be safe to run multiple times so that re-ingestion does not create duplicate records.

**Acceptance Criteria**
- AC-3.1: Re-ingesting the same product updates the existing record rather than inserting a duplicate
- AC-3.2: `product_uid` is enforced as a unique business key at the database level

### US-4: Handle database failures gracefully
As an operator, I want a clear error response when the database is unavailable so that I can diagnose the problem quickly.

**Acceptance Criteria**
- AC-4.1: A `DatabaseException` results in a `503 Service Unavailable` response
- AC-4.2: The response body contains the exception message

## Correctness Properties

These properties must hold for all valid inputs and are validated by property-based tests.

| ID | Property |
|---|---|
| CP-1 | For every product returned by the products API, exactly one record exists in the database after ingestion |
| CP-2 | For every product with a matching price entry, `unitPrice` equals the price from the last occurrence in the price feed |
| CP-3 | For every product with no matching price entry, `unitPrice` is `null` |
| CP-4 | Running ingestion N times with the same data produces the same database state as running it once |
| CP-5 | No two persisted products share the same `product_uid` |
