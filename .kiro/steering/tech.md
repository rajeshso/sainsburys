# Tech Stack

## Language & Runtime
- Java 21
- Spring Boot 3.x
- Gradle (Kotlin or Groovy DSL)

## Core Dependencies
- `spring-boot-starter-web` — REST API layer
- `spring-boot-starter-data-jpa` — ORM / persistence
- `lombok` — boilerplate reduction (`@Getter`, `@Setter`, `@Builder`, `@Slf4j`, etc.)
- `spring-boot-devtools` — development-time hot reload

## Database
- Production: PostgreSQL (via `org.postgresql:postgresql`)
- Tests: H2 in-memory (`com.h2database:h2`) for `@DataJpaTest` slices
- Integration tests: Testcontainers (`org.testcontainers:postgresql`) for full-stack scenarios
- Schema management: `spring.jpa.hibernate.ddl-auto=create-drop` (dev/test); migrate to Flyway or Liquibase for production

## HTTP Client
- `java.net.http.HttpClient` (JDK built-in) for outbound REST calls
- Wrap in a `@Component` that implements a typed interface for testability

## Testing
- JUnit 5 (`junit-jupiter`) via `spring-boot-starter-test`
- Mockito (`MockitoExtension`, `@Mock`, `@InjectMocks`) for unit tests
- `@WebMvcTest` + `MockMvc` for controller slice tests
- `@DataJpaTest` + H2 for repository slice tests
- `@SpringBootTest` + Testcontainers for integration tests
- `@MockitoBean` (Spring Boot 3.4+) replaces deprecated `@MockBean`

## Code Style Conventions
- Use Java records for immutable DTOs / response models
- Use `@Builder` on JPA entities; keep entities free of business logic
- Use `@NaturalId` + `@NaturalIdCache` for business-key lookups
- Prefer constructor injection over field injection
- Use `@Slf4j` for logging; log at `INFO` for lifecycle events, `DEBUG` for detail
- All public service methods should be `@Transactional` where they mutate state

## Build & Run
```bash
./gradlew build          # compile + test
./gradlew test           # run tests only
./gradlew bootRun        # start dev server
```
