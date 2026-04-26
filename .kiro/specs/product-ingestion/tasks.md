# Tasks

## Implementation Plan

- [ ] 1. Implement `ProductApiClient` HTTP calls
  - [ ] 1.1 Inject product and price URLs via `@Value` from `application.properties`
  - [ ] 1.2 Implement `fetchProducts()` — GET request, deserialise JSON array into `List<ProductResponse>`
  - [ ] 1.3 Implement `fetchPrices()` — GET request, deserialise JSON array into `List<ProductPriceResponse>`
  - [ ] 1.4 Add error handling for non-2xx responses (throw `RuntimeException` with status code)

- [ ] 2. Implement upsert logic in `ProductService`
  - [ ] 2.1 Resolve existing products by `productUid` before `saveAll` to enable true upsert-by-natural-key
  - [ ] 2.2 Update fields on existing entity or build new entity when not found
  - [ ] 2.3 Wrap persistence errors in `DatabaseException`

- [ ] 3. Write service unit tests (`ProductServiceTest`)
  - [ ] 3.1 New product with matching price → persisted with correct `unitPrice`
  - [ ] 3.2 Product with no price match → persisted with `unitPrice = null`
  - [ ] 3.3 Duplicate `product_uid` in price feed → last-write-wins
  - [ ] 3.4 Calling `ingestProducts()` twice with same data → `saveAll` called twice, same result

- [ ] 4. Write controller slice tests (`ProductControllerTestUnit`)
  - [ ] 4.1 `POST /products/ingest` → `202 Accepted`
  - [ ] 4.2 Service throws `DatabaseException` → `503 Service Unavailable` with message body

- [ ] 5. Write repository slice tests (`ProductRepositoryTest`)
  - [ ] 5.1 Save and find by `productUid` → fields match
  - [ ] 5.2 Duplicate `productUid` → `DataIntegrityViolationException`
  - [ ] 5.3 Update existing product → changes reflected after flush

- [ ] 6. Write property-based tests
  - [ ] 6.1 CP-1: For any list of products, every `product_uid` appears exactly once after ingestion
  - [ ] 6.2 CP-2: For any product with a price match, `unitPrice` equals the last price entry for that uid
  - [ ] 6.3 CP-3: For any product with no price match, `unitPrice` is `null`
  - [ ] 6.4 CP-4: Running ingestion N times with the same input produces the same final state
  - [ ] 6.5 CP-5: No two persisted products share the same `product_uid`

- [ ] 7. Integration test (`@SpringBootTest` + Testcontainers)
  - [ ] 7.1 Start PostgreSQL container, call `POST /products/ingest` with mocked API client, assert DB state
  - [ ] 7.2 Call endpoint twice, assert no duplicates in DB

- [ ]* 8. Externalise configuration
  - [ ]* 8.1 Move hardcoded API URLs to `application.properties`
  - [ ]* 8.2 Add Flyway or Liquibase migration to replace `create-drop` in production profile
