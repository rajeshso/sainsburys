# Design

## Architecture Overview

```
POST /products/ingest
        │
        ▼
ProductController
        │  delegates
        ▼
ProductService  ──────────────────────────────────────────┐
        │  fetchProducts()          fetchPrices()          │
        ▼                                                  │
ProductApiClient (implements ApiClient)                    │
        │  java.net.http.HttpClient                        │
        ▼                                                  │
External JSON APIs                                         │
        │                                                  │
        └──── merge by product_uid (in-memory) ───────────┘
                        │
                        ▼
              ProductRepository.saveAll(...)
                        │
                        ▼
                  PostgreSQL (prod)
                  H2 (test slices)
```

## Components

### `ProductController`
- `@RestController`, `@RequestMapping("/products")`
- Single endpoint: `POST /ingest` → calls `productService.ingestProducts()` → returns `ResponseEntity.accepted().build()`
- No business logic; exception handling delegated to `GlobalExceptionHandler`

### `GlobalExceptionHandler`
- `@ControllerAdvice`
- Maps `DatabaseException` → `503 Service Unavailable` with message body
- Can be extended for additional exception types without touching controllers

### `ProductService`
- `@Service`, `@Slf4j`
- `@Transactional` on `ingestProducts()`
- Algorithm:
  1. `List<ProductResponse> products = client.fetchProducts()`
  2. `Map<String, Double> priceByUid = client.fetchPrices()` → `Collectors.toMap(..., (a, b) -> b)`
  3. Stream `products` → map each to `Product` entity using `priceByUid.get(uid)` (null-safe)
  4. `productRepository.saveAll(entities)`

### `ApiClient` (interface)
```java
List<ProductResponse> fetchProducts() throws MalformedURLException;
List<ProductPriceResponse> fetchPrices() throws MalformedURLException;
```
Decouples the service from the HTTP implementation — enables mocking in unit tests.

### `ProductApiClient`
- `@Component`, implements `ApiClient`
- Uses `java.net.http.HttpClient` to call the two S3-hosted JSON endpoints
- Deserialises responses into `List<ProductResponse>` and `List<ProductPriceResponse>`

### `Product` (entity)
```java
@Entity @Builder @NaturalIdCache
class Product {
    @Id @GeneratedValue Long id;
    @NaturalId String productUid;   // unique, immutable business key
    String productType;
    String name;
    String fullUrl;
    Double unitPrice;               // nullable
}
```

### DTOs (Java records)
```java
record ProductResponse(String product_uid, String product_type, String name, String fullUrl, Double unitPrice) {}
record ProductPriceResponse(String product_uid, double unit_price, String unit_price_measure, int unit_price_measure_amount) {}
```

### `ProductRepository`
```java
interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByProductUid(String productUid);
}
```

## Data Flow — Merge Logic

```
products  = [{ uid:"A", name:"Salmon" }, { uid:"B", name:"Bread" }]
prices    = [{ uid:"A", price:15.63 },  { uid:"A", price:12.00 }]  // duplicate uid

priceByUid = { "A" → 12.00 }   // last-write-wins via merge function

toSave = [
  Product{ uid:"A", unitPrice:12.00 },
  Product{ uid:"B", unitPrice:null }   // no price match
]
```

## Test Strategy

| Layer | Tool | Scope |
|---|---|---|
| Service unit | Mockito `@ExtendWith(MockitoExtension.class)` | Merge logic, null price, duplicate price, idempotency call count |
| Controller slice | `@WebMvcTest` + `MockMvc` | HTTP status codes, error mapping |
| Repository slice | `@DataJpaTest` + H2 | Save, find-by-uid, duplicate constraint, upsert |
| Integration | `@SpringBootTest` + Testcontainers (PostgreSQL) | Full stack end-to-end |
| Property-based | jqwik or similar | CP-1 through CP-5 from requirements |

## Key Design Decisions

- **`saveAll` for upsert**: JPA merge semantics handle insert-or-update when the entity already exists by surrogate PK. For true upsert-by-natural-key, the service must first resolve existing entities by `productUid` before calling `saveAll`, or use a native `ON CONFLICT DO UPDATE` query.
- **In-memory join**: Both datasets are small enough to hold in memory; no streaming or pagination needed at this scale.
- **Null price is intentional**: The spec explicitly allows products without prices — `Double` (boxed) is used instead of `double` to represent this.
- **Interface for client**: `ApiClient` interface allows `@MockitoBean` / `@Mock` substitution in all test layers without starting a real HTTP server.
