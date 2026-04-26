# Project Structure

## Package Layout

```
src/
├── main/
│   ├── java/{base-package}/
│   │   ├── {AppName}Application.java       # Spring Boot entry point
│   │   ├── client/                         # Outbound HTTP clients
│   │   │   ├── ApiClient.java              # Interface defining fetch methods
│   │   │   └── {Domain}ApiClient.java      # Concrete implementation
│   │   ├── controller/                     # REST controllers + exception handlers
│   │   │   ├── {Domain}Controller.java
│   │   │   └── GlobalExceptionHandler.java
│   │   ├── exception/                      # Custom runtime exceptions
│   │   │   └── {Name}Exception.java
│   │   ├── model/                          # JPA entities + record DTOs
│   │   │   ├── {Entity}.java               # @Entity with @Builder, @NaturalId
│   │   │   └── {Domain}Response.java       # Java record (immutable DTO)
│   │   ├── repository/                     # Spring Data JPA repositories
│   │   │   └── {Entity}Repository.java
│   │   └── service/                        # Business logic, @Transactional
│   │       └── {Domain}Service.java
│   └── resources/
│       └── application.properties
└── test/
    ├── java/{base-package}/
    │   ├── controller/                     # @WebMvcTest slice tests
    │   ├── repository/                     # @DataJpaTest slice tests (H2)
    │   └── service/                        # Mockito unit tests
    └── resources/
        └── application.properties          # Test-specific overrides
```

## Naming Conventions

| Artifact | Pattern | Example |
|---|---|---|
| Entity | `PascalCase` noun | `Product` |
| Repository | `{Entity}Repository` | `ProductRepository` |
| Service | `{Domain}Service` | `ProductService` |
| Controller | `{Domain}Controller` | `ProductController` |
| API Client interface | `ApiClient` | `ApiClient` |
| API Client impl | `{Domain}ApiClient` | `ProductApiClient` |
| DTO (record) | `{Domain}Response` | `ProductResponse` |
| Exception | `{Context}Exception` | `DatabaseException` |
| Unit test | `{Class}Test` | `ProductServiceTest` |
| Controller test | `{Class}TestUnit` | `ProductControllerTestUnit` |

## Layering Rules

- Controllers delegate entirely to services — no business logic in controllers
- Services own all business logic and transaction boundaries
- Repositories are pure Spring Data interfaces — no custom SQL unless necessary
- Clients are `@Component` beans implementing a typed interface — never called directly from controllers
- Exceptions are thrown from services/clients and caught by `GlobalExceptionHandler`

## Key Patterns

- **Upsert via `saveAll`**: rely on JPA merge semantics with `@NaturalId` for idempotent writes
- **Merge by business key**: fetch both data sources, join in-memory by UID, then persist
- **Null-safe price mapping**: products without a matching price entry persist with `null` unit price
- **Last-write-wins for duplicates**: use `Collectors.toMap(..., (a, b) -> b)` merge function
