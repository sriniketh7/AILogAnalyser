# Spring Framework & Spring Boot Exceptions Reference (RAG Knowledge Base)

Companion file to `java_exceptions_reference.md`. Covers exceptions specific to Spring Core, Spring MVC/WebFlux, Spring Data, and Spring Transaction management. Same schema: Type, Package, Description, Common Causes, Symptoms, Example Stack Trace, Common Fixes.

---

## 1. Spring Core / Bean Container Exceptions

### 1.1 BeanCreationException
- **Package:** `org.springframework.beans.factory`
- **Description:** Thrown when the Spring container fails to create a bean instance, usually wrapping a more specific root cause.
- **Common Causes:**
  - Constructor/`@PostConstruct` method throws an exception
  - Required property or dependency not set
  - Misconfigured `@Value` placeholder resolving to nothing
- **Symptoms:** Application fails to start; stack trace shows a long chain of "Error creating bean with name..." messages.
- **Example Stack Trace:**
```
org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'orderService' defined in file [OrderService.class]: Unsatisfied dependency expressed through constructor parameter 0
    at org.springframework.beans.factory.support.ConstructorResolver.createArgumentArray(ConstructorResolver.java:800)
Caused by: org.springframework.beans.factory.NoSuchBeanDefinitionException: No qualifying bean of type 'com.example.repository.OrderRepository' available
```
- **Common Fixes:**
  - Read the full "Caused by" chain — the root cause is usually several levels down
  - Verify the missing/misconfigured bean is annotated (`@Component`, `@Service`, `@Repository`) and component-scanned
  - Check `@Value`/`application.properties` keys for typos

---

### 1.2 NoSuchBeanDefinitionException
- **Package:** `org.springframework.beans.factory`
- **Description:** Thrown when Spring cannot find a bean matching the requested type/name for autowiring or lookup.
- **Common Causes:**
  - Missing `@Component`/`@Service`/`@Repository`/`@Bean` definition
  - Bean's package not covered by `@ComponentScan`
  - Conditional bean (`@ConditionalOnProperty`, profile-specific) not activated in current profile
- **Symptoms:** Fails at startup during dependency injection, or at runtime if using `ApplicationContext.getBean()` lazily.
- **Example Stack Trace:**
```
org.springframework.beans.factory.NoSuchBeanDefinitionException: No qualifying bean of type 'com.example.service.NotificationService' available: expected at least 1 bean which qualifies as autowire candidate
    at org.springframework.beans.factory.support.DefaultListableBeanFactory.raiseNoMatchingBeanFound(DefaultListableBeanFactory.java:1826)
```
- **Common Fixes:**
  - Confirm the class has a stereotype annotation and lives under the base package scanned by `@SpringBootApplication`
  - Check active profiles (`spring.profiles.active`) match the bean's `@Profile` condition
  - For multiple implementations of an interface, use `@Qualifier` or `@Primary`

---

### 1.3 NoUniqueBeanDefinitionException
- **Package:** `org.springframework.beans.factory`
- **Description:** Thrown when Spring finds more than one candidate bean for an autowire point that expects exactly one.
- **Common Causes:**
  - Multiple implementations of the same interface without a `@Primary` or `@Qualifier` to disambiguate
  - Test configuration accidentally registering a duplicate bean alongside the production one
- **Symptoms:** Fails at startup, specifically pointing to the ambiguous injection point.
- **Example Stack Trace:**
```
org.springframework.beans.factory.NoUniqueBeanDefinitionException: No qualifying bean of type 'com.example.service.PaymentGateway' available: expected single matching bean but found 2: stripeGateway,razorpayGateway
    at org.springframework.beans.factory.support.DefaultListableBeanFactory.doResolveDependency(DefaultListableBeanFactory.java:1653)
```
- **Common Fixes:**
  - Add `@Primary` to the default implementation
  - Use `@Qualifier("beanName")` at each injection point needing a specific implementation
  - Consider injecting `List<PaymentGateway>` if all implementations should be used together

---

### 1.4 BeanCurrentlyInCreationException
- **Package:** `org.springframework.beans.factory`
- **Description:** Thrown when Spring detects a circular dependency it cannot resolve (commonly with constructor injection).
- **Common Causes:**
  - Bean A's constructor requires Bean B, and Bean B's constructor requires Bean A
  - Circular chain across three or more beans
- **Symptoms:** Fails at startup with "Requested bean is currently in creation" — often only appears after switching from field/setter injection to constructor injection.
- **Example Stack Trace:**
```
org.springframework.beans.factory.BeanCurrentlyInCreationException: Error creating bean with name 'userService': Requested bean is currently in creation: Is there an unresolvable circular reference?
    at org.springframework.beans.factory.support.DefaultSingletonBeanRegistry.beforeSingletonCreation(DefaultSingletonBeanRegistry.java:358)
```
- **Common Fixes:**
  - Refactor to remove the circular dependency (usually indicates a design smell — extract shared logic into a third bean)
  - As a stopgap, use `@Lazy` on one of the constructor parameters
  - Switch to setter injection for one side only as a last resort (constructor injection is still preferred for immutability)

---

### 1.5 UnsatisfiedDependencyException
- **Package:** `org.springframework.beans.factory`
- **Description:** Thrown when Spring cannot satisfy a required constructor or setter dependency during bean instantiation.
- **Common Causes:**
  - A required dependency bean doesn't exist or failed to initialize itself
  - Wrong parameter types in constructor vs. available beans
- **Symptoms:** Startup failure, usually chained under a `BeanCreationException`.
- **Example Stack Trace:**
```
org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'reportController': Unsatisfied dependency expressed through constructor parameter 1
Caused by: org.springframework.beans.factory.NoSuchBeanDefinitionException: No qualifying bean of type 'com.example.service.ReportService' available
```
- **Common Fixes:**
  - Same as `NoSuchBeanDefinitionException` — trace to the actual missing bean
  - Double-check constructor parameter order matches intended autowiring

---

### 1.6 BeanInstantiationException
- **Package:** `org.springframework.beans`
- **Description:** Thrown when Spring fails to instantiate a bean class itself (as opposed to dependency resolution issues) — e.g., no default constructor found, or the constructor threw an exception.
- **Common Causes:**
  - Class is abstract or an interface mistakenly registered as a concrete bean
  - Constructor requires arguments Spring cannot supply
  - Reflection-based instantiation blocked (e.g., private constructor without proper access)
- **Symptoms:** Fails at startup during component instantiation.
- **Example Stack Trace:**
```
org.springframework.beans.BeanInstantiationException: Failed to instantiate [com.example.config.CacheConfig]: No default constructor found
    at org.springframework.beans.factory.support.SimpleInstantiationStrategy.instantiate(SimpleInstantiationStrategy.java:100)
```
- **Common Fixes:**
  - Add a no-arg constructor if Spring needs to instantiate directly, or use `@Bean` factory methods instead
  - Ensure `@Configuration` classes aren't marked `final` (CGLIB proxying requirement in some Spring versions)

---

## 2. Spring MVC / Web Layer Exceptions

### 2.1 HttpMessageNotReadableException
- **Package:** `org.springframework.http.converter`
- **Description:** Thrown when Spring cannot deserialize the incoming HTTP request body into the target Java object (typically JSON parsing failure).
- **Common Causes:**
  - Malformed JSON in request body
  - Field type mismatch (e.g., client sends a string where a number is expected)
  - Missing request body entirely on an endpoint expecting one
- **Symptoms:** Client receives HTTP 400 immediately; request never reaches controller method body.
- **Example Stack Trace:**
```
org.springframework.http.converter.HttpMessageNotReadableException: JSON parse error: Unexpected character ('}' (code 125)): was expecting double-quote to start field name
    at org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter.readJavaType(AbstractJackson2HttpMessageConverter.java:389)
    at com.example.controller.OrderController.createOrder(OrderController.java:1)
```
- **Common Fixes:**
  - Validate JSON payload structure with a schema or client-side validation
  - Use `@JsonProperty`/proper DTO field types matching client contract
  - Add a `@ControllerAdvice` handler to return a clean 400 response with details instead of a raw stack trace

---

### 2.2 MethodArgumentNotValidException
- **Package:** `org.springframework.web.bind`
- **Description:** Thrown when `@Valid`/`@Validated` bean validation fails on a controller method argument (typically `@RequestBody`).
- **Common Causes:**
  - Client submits data violating `@NotNull`, `@Size`, `@Email`, `@Pattern`, etc.
- **Symptoms:** HTTP 400 response with field-level validation error details (if handled) or a raw exception trace (if not handled).
- **Example Stack Trace:**
```
org.springframework.web.bind.MethodArgumentNotValidException: Validation failed for argument [0] in public com.example.dto.OrderResponse com.example.controller.OrderController.createOrder(com.example.dto.OrderRequest): [Field error in object 'orderRequest' on field 'quantity': rejected value [-1]; ... default message [must be greater than 0]]
    at org.springframework.web.method.annotation.ModelAttributeMethodProcessor.resolveArgument(ModelAttributeMethodProcessor.java:170)
```
- **Common Fixes:**
  - Add an `@ExceptionHandler(MethodArgumentNotValidException.class)` in a `@ControllerAdvice` to format field errors into a clean API response
  - Keep DTO validation annotations aligned with actual business rules
  - Return `BindingResult.getFieldErrors()` details to help API consumers fix their request

---

### 2.3 MissingServletRequestParameterException
- **Package:** `org.springframework.web.bind`
- **Description:** Thrown when a required `@RequestParam` is missing from the incoming request.
- **Common Causes:**
  - Client omits a required query parameter
  - `@RequestParam` not marked `required = false` when it should be optional
- **Symptoms:** HTTP 400 with a message naming the missing parameter.
- **Example Stack Trace:**
```
org.springframework.web.bind.MissingServletRequestParameterException: Required request parameter 'status' for method parameter type String is not present
    at org.springframework.web.method.annotation.RequestParamMethodArgumentResolver.handleMissingValue(RequestParamMethodArgumentResolver.java:227)
```
- **Common Fixes:**
  - Set `required = false` with a sensible default for genuinely optional parameters
  - Document required query parameters clearly in API contract/OpenAPI spec

---

### 2.4 HttpRequestMethodNotSupportedException
- **Package:** `org.springframework.web`
- **Description:** Thrown when the HTTP method used (GET, POST, etc.) doesn't match any handler mapped for that URL.
- **Common Causes:**
  - Client calls `DELETE` on an endpoint that only supports `GET`/`POST`
  - Typo or mismatch between frontend and backend route definitions
- **Symptoms:** HTTP 405 Method Not Allowed response.
- **Example Stack Trace:**
```
org.springframework.web.HttpRequestMethodNotSupportedException: Request method 'DELETE' not supported
    at org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping.handleNoMatch(RequestMappingInfoHandlerMapping.java:242)
```
- **Common Fixes:**
  - Verify the controller has a handler mapped for the intended HTTP method
  - Check API documentation/frontend code for method mismatches

---

### 2.5 NoHandlerFoundException
- **Package:** `org.springframework.web.servlet`
- **Description:** Thrown when no controller handler matches the requested URL path at all (true 404).
- **Common Causes:**
  - Typo in the URL path
  - Missing `@RequestMapping`/`@GetMapping` on the intended controller
  - `spring.mvc.throw-exception-if-no-handler-found=true` needed to surface this instead of a default 404 page
- **Symptoms:** HTTP 404; only thrown as a catchable exception if explicitly configured.
- **Example Stack Trace:**
```
org.springframework.web.servlet.NoHandlerFoundException: No handler found for GET /api/v1/orders/status
    at org.springframework.web.servlet.DispatcherServlet.noHandlerFound(DispatcherServlet.java:1250)
```
- **Common Fixes:**
  - Verify route path spelling and base path (`context-path`) configuration
  - Enable `throw-exception-if-no-handler-found` and disable default resource handling if custom 404 handling is desired

---

### 2.6 AsyncRequestTimeoutException
- **Package:** `org.springframework.web.context.request.async`
- **Description:** Thrown when an async request (e.g., `DeferredResult`, `Callable`, `StreamingResponseBody`) exceeds Spring MVC's configured async timeout before completing.
- **Common Causes:**
  - Long-running downstream call (large query, external API) exceeding `spring.mvc.async.request-timeout`
  - Streaming large result sets (e.g., 30k+ records) where per-chunk processing/flush latency accumulates past the timeout
  - Client-side connection dropped, but server-side async processing continues past its own timeout
- **Symptoms:** HTTP 503/timeout on client for large exports or slow endpoints; server logs show the async context timing out mid-stream.
- **Example Stack Trace:**
```
org.springframework.web.context.request.async.AsyncRequestTimeoutException: null
    at org.springframework.web.context.request.async.TimeoutDeferredResultProcessingInterceptor.handleTimeout(TimeoutDeferredResultProcessingInterceptor.java:42)
    at org.springframework.web.context.request.async.DeferredResult.setErrorResult(DeferredResult.java:224)
```
- **Common Fixes:**
  - Increase `spring.mvc.async.request-timeout` for legitimately long streaming operations, sized to actual expected duration
  - For `StreamingResponseBody` with GZIP, ensure `GZIPOutputStream` is flushed periodically per chunk rather than only at stream close, so the client sees continuous progress instead of a long silent buffering window
  - Switch offset-based pagination to keyset/cursor-based pagination for large result sets to keep per-page query time flat rather than degrading as offset grows
  - Add server-side progress logging to distinguish "slow query" vs. "slow serialization/compression" as the root cause

---

## 3. Spring Data Access Exceptions

### 3.1 CannotCreateTransactionException
- **Package:** `org.springframework.transaction`
- **Description:** Thrown when Spring's transaction manager cannot open a new transaction, typically due to an underlying connection failure.
- **Common Causes:**
  - Database is down or unreachable
  - Connection pool exhausted (all connections checked out)
  - Network partition between app and DB
- **Symptoms:** Every transactional operation fails simultaneously; often accompanies a broader outage.
- **Example Stack Trace:**
```
org.springframework.transaction.CannotCreateTransactionException: Could not open JPA EntityManager for transaction
Caused by: org.hibernate.exception.JDBCConnectionException: Unable to acquire JDBC Connection
Caused by: java.sql.SQLTransientConnectionException: HikariPool-1 - Connection is not available, request timed out after 30000ms.
```
- **Common Fixes:**
  - Check DB server health and network connectivity first
  - Review connection pool sizing and leak detection settings
  - Add retry/backoff at the application level for transient DB unavailability

---

### 3.2 TransactionSystemException
- **Package:** `org.springframework.transaction`
- **Description:** Thrown when an error occurs during transaction commit or rollback itself (not the business logic within it).
- **Common Causes:**
  - Constraint violation only detected at flush/commit time (deferred constraints)
  - JPA validation failure triggered during flush (e.g., `@PrePersist` callback throws)
- **Symptoms:** Business logic appears to succeed, but the transaction fails at the very end, often confusing to debug.
- **Example Stack Trace:**
```
org.springframework.transaction.TransactionSystemException: Could not commit JPA transaction
Caused by: jakarta.persistence.RollbackException: Error while committing the transaction
Caused by: jakarta.validation.ConstraintViolationException: Validation failed for classes [com.example.model.Order]
```
- **Common Fixes:**
  - Read the deepest "Caused by" — it's rarely about the transaction mechanism itself
  - Move validation earlier (DTO-level `@Valid`) so failures surface before reaching commit
  - Check for `@PrePersist`/`@PreUpdate` lifecycle callbacks that might throw

---

### 3.3 DataAccessResourceFailureException
- **Package:** `org.springframework.dao`
- **Description:** Spring's translated exception for resource-level failures accessing the underlying persistence resource (e.g., connection failures translated from vendor-specific exceptions).
- **Common Causes:**
  - Database server restarted or became unreachable mid-operation
  - Connection pool misconfiguration
- **Symptoms:** Sudden failures across multiple unrelated repository calls at the same time.
- **Example Stack Trace:**
```
org.springframework.dao.DataAccessResourceFailureException: unable to obtain isolated JDBC connection
    at org.springframework.orm.jpa.vendor.HibernateJpaDialect.convertHibernateAccessException(HibernateJpaDialect.java:266)
```
- **Common Fixes:**
  - Verify database availability and connection string configuration
  - Add health checks and readiness probes so the app doesn't route traffic during DB outages

---

### 3.4 QueryTimeoutException
- **Package:** `org.springframework.dao`
- **Description:** Spring's translated exception when a query exceeds its configured timeout before completing.
- **Common Causes:**
  - Missing or inefficient index causing full table scan on a large table
  - Query timeout configured too aggressively for legitimate large-dataset queries
  - Lock contention delaying query execution
- **Symptoms:** Fails on specific "heavy" queries (large joins, aggregations, offset-based pagination at high offsets) while simple queries succeed.
- **Example Stack Trace:**
```
org.springframework.dao.QueryTimeoutException: Statement cancelled due to timeout
Caused by: org.postgresql.util.PSQLException: ERROR: canceling statement due to statement timeout
```
- **Common Fixes:**
  - Add `EXPLAIN ANALYZE` to identify missing indexes or bad query plans
  - Replace offset-based pagination with keyset pagination for deep pages on large tables
  - Increase timeout only after confirming the query is genuinely necessary and optimized

---

### 3.5 DuplicateKeyException
- **Package:** `org.springframework.dao`
- **Description:** Spring's translated exception specifically for unique constraint/index violations on insert or update.
- **Common Causes:**
  - Inserting a row with a value that already exists in a unique-constrained column
  - Race condition: two concurrent requests both pass an application-level uniqueness check before either has committed
- **Symptoms:** Fails on `save()`/`insert()` calls with specific duplicate data.
- **Example Stack Trace:**
```
org.springframework.dao.DuplicateKeyException: could not execute statement; SQL [n/a]; constraint [uk_username]
Caused by: org.postgresql.util.PSQLException: ERROR: duplicate key value violates unique constraint "uk_username"
```
- **Common Fixes:**
  - Rely on the DB constraint as the source of truth rather than only an application-level pre-check (to close race-condition windows)
  - Catch and translate into a clear "already exists" API error (HTTP 409)
  - Consider `INSERT ... ON CONFLICT DO NOTHING/UPDATE` (Postgres upsert) for idempotent writes

---

### 3.6 DeadlockLoserDataAccessException
- **Package:** `org.springframework.dao`
- **Description:** Spring's translated exception when the database detects a deadlock and this transaction was chosen as the "victim" to roll back.
- **Common Causes:**
  - Two transactions acquiring the same rows/locks in different order
  - High-contention hotspots (e.g., same inventory row updated by many concurrent transactions)
- **Symptoms:** Intermittent failures under concurrent write load; retrying the same operation usually succeeds.
- **Example Stack Trace:**
```
org.springframework.dao.DeadlockLoserDataAccessException: could not execute statement
Caused by: org.postgresql.util.PSQLException: ERROR: deadlock detected
  Detail: Process 1234 waits for ShareLock on transaction 5678; blocked by process 5678.
```
- **Common Fixes:**
  - Standardize the order in which rows/tables are locked/updated across all transactions
  - Add automatic retry with backoff specifically for this exception type
  - Reduce transaction scope/duration to shrink the lock contention window

---

### 3.7 InvalidDataAccessApiUsageException
- **Package:** `org.springframework.dao`
- **Description:** Thrown when Spring Data/JPA APIs are used incorrectly at the application code level (not a data problem itself).
- **Common Causes:**
  - Passing a detached entity where a managed one is expected
  - Calling repository methods with malformed or nonsensical arguments (e.g., invalid `Pageable`)
  - Mismatched query method naming convention in a Spring Data repository interface
- **Symptoms:** Fails consistently (not intermittently) — a code-level bug rather than a data/environment issue.
- **Example Stack Trace:**
```
org.springframework.dao.InvalidDataAccessApiUsageException: org.hibernate.query.SemanticException: Path expected for interpretation of terminal as an entity relies on entity name resolution but the following was resolved to a non-entity
    at com.example.repository.OrderRepository.findByCustomer_EmailAndStatus(OrderRepository.java:1)
```
- **Common Fixes:**
  - Verify Spring Data derived query method names precisely match entity field names (case-sensitive, correct nesting with `_`)
  - Use `@Query` with explicit JPQL when derived query naming becomes ambiguous or too complex
  - Test repository methods in isolation with `@DataJpaTest`

---

## 4. Quick-Reference Decision Table

| Symptom | Likely Exception | First Thing to Check |
|---|---|---|
| App won't start, "Error creating bean" | BeanCreationException | Follow the "Caused by" chain to the root |
| App won't start, ambiguous injection | NoUniqueBeanDefinitionException | Add `@Primary`/`@Qualifier` |
| App won't start, circular reference | BeanCurrentlyInCreationException | Break the cycle or use `@Lazy` |
| POST/PUT request fails with 400 immediately | HttpMessageNotReadableException | JSON payload structure/types |
| POST/PUT fails with 400 and field errors | MethodArgumentNotValidException | DTO `@Valid` annotations |
| Large export/stream times out mid-transfer | AsyncRequestTimeoutException | `spring.mvc.async.request-timeout`, GZIP flush cadence, pagination strategy |
| All DB calls suddenly fail at once | CannotCreateTransactionException / DataAccessResourceFailureException | DB health, connection pool exhaustion |
| Insert/update fails with duplicate data | DuplicateKeyException | Unique constraint, race condition on pre-check |
| Concurrent updates fail intermittently, retry works | DeadlockLoserDataAccessException / OptimisticLockException | Lock ordering, transaction scope |
| Heavy query fails only at large offsets | QueryTimeoutException | Pagination strategy, missing index |

---

*Document scope: Spring Core (bean container), Spring MVC/Web, and Spring Data/Transaction exceptions. Companion to `java_exceptions_reference.md` and `postgresql_errors_reference.md`. Intended for RAG retrieval.*
