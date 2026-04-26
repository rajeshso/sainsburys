# Product Context

## Domain
A Spring Boot microservice that ingests product and pricing data from external APIs, merges them by a business key (`product_uid`), and persists the result to a relational database.

## Core Capability
- Fetch product catalogue and price data from two separate external JSON endpoints
- Merge the two datasets in-memory using `product_uid` as the join key
- Upsert the merged records into the database (idempotent — safe to call repeatedly)
- Expose a single REST endpoint to trigger ingestion on demand

## Domain Entities

### Product
The central entity. Represents a single product with its pricing information.

| Field | Type | Notes |
|---|---|---|
| `id` | `Long` | Auto-generated surrogate PK |
| `productUid` | `String` | Business key (`@NaturalId`), unique, immutable |
| `productType` | `String` | Category/type label |
| `name` | `String` | Display name |
| `fullUrl` | `String` | Link to product page |
| `unitPrice` | `Double` | Nullable — absent if no price data available |

## API Contract

### Ingest Endpoint
```
POST /products/ingest
```
- Triggers a full fetch-merge-upsert cycle
- Returns `202 Accepted` on success
- Returns `503 Service Unavailable` with error message body if the database is unavailable

## External Data Sources
- Products endpoint: returns a list of `{ product_uid, product_type, name, fullUrl }`
- Prices endpoint: returns a list of `{ product_uid, unit_price, unit_price_measure, unit_price_measure_amount }`
- Both are read-only; the service never writes back to them

## Business Rules
1. Products without a matching price entry are persisted with `unitPrice = null`
2. If a product UID appears multiple times in the price feed, the last entry wins
3. Ingestion is idempotent — calling the endpoint multiple times produces the same final state
4. The service does not expose a read/query API — ingestion only

## Error Handling
- `DatabaseException` (unchecked) signals persistence failures → mapped to `503` by `GlobalExceptionHandler`
- External API failures should propagate as runtime exceptions and be handled at the controller advice level
