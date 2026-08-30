# PostgreSQL Errors Reference (RAG Knowledge Base)

Companion file to `java_exceptions_reference.md` and `spring_exceptions_reference.md`. PostgreSQL identifies errors by a 5-character SQLSTATE code rather than an exception class name — this file is organized by SQLSTATE, with the Java/JDBC exception it typically surfaces as. Same schema: Code/Name, Description, Common Causes, Symptoms, Example, Common Fixes.

---

## 1. Class 23 — Integrity Constraint Violations

### 1.1 23505 — unique_violation
- **Description:** A unique index or unique constraint was violated by an insert or update.
- **Common Causes:**
  - Duplicate value inserted into a column/composite with a `UNIQUE` constraint
  - Race condition: two concurrent transactions both pass an application-level "check if exists" before either commits
  - Retrying a failed insert without checking whether the original attempt actually succeeded
- **Symptoms:** Fails on specific insert/update statements with duplicate data; often intermittent under concurrent load even when application-level pre-checks exist.
- **Example:**
```
ERROR: duplicate key value violates unique constraint "uk_users_email"
DETAIL: Key (email)=(jane@example.com) already exists.
```
  Surfaces in Java as: `org.postgresql.util.PSQLException` → Spring's `DuplicateKeyException` / Hibernate's `ConstraintViolationException`.
- **Common Fixes:**
  - Use `INSERT ... ON CONFLICT (column) DO NOTHING` or `DO UPDATE` for idempotent upserts
  - Don't rely solely on a pre-check query — let the DB constraint be the final source of truth and handle the exception
  - Add a friendly API-level 409 Conflict response mapped from this error

---

### 1.2 23503 — foreign_key_violation
- **Description:** An insert/update/delete violated a foreign key constraint — referencing a non-existent parent row, or deleting a parent row still referenced by children.
- **Common Causes:**
  - Inserting a child row with a foreign key value that doesn't exist in the parent table
  - Deleting a parent row without `ON DELETE CASCADE`/`SET NULL` while child rows still reference it
  - Race condition where the parent row is deleted between the app's existence check and the child insert
- **Symptoms:** Fails on specific insert/delete operations depending on referential state.
- **Example:**
```
ERROR: insert or update on table "orders" violates foreign key constraint "fk_orders_customer"
DETAIL: Key (customer_id)=(9981) is not present in table "customers".
```
- **Common Fixes:**
  - Verify the referenced parent row exists before insert, or rely on the constraint and handle the error gracefully
  - Decide explicit `ON DELETE` behavior (`CASCADE`, `SET NULL`, `RESTRICT`) matching business rules
  - Wrap parent-then-child inserts in a single transaction to avoid partial states

---

### 1.3 23502 — not_null_violation
- **Description:** An insert/update attempted to set a `NOT NULL` column to null.
- **Common Causes:**
  - Application code omits a required field, or maps it incorrectly (e.g., empty string vs. null confusion)
  - Column made `NOT NULL` after existing code paths were written to allow nulls
- **Symptoms:** Fails at insert/update time for specific rows missing required data.
- **Example:**
```
ERROR: null value in column "created_at" of relation "audit_log" violates not-null constraint
DETAIL: Failing row contains (1042, null, ...).
```
- **Common Fixes:**
  - Add `@NotNull`/Bean Validation at the DTO layer to catch this before it reaches the DB
  - Provide a default value (`DEFAULT now()` at the DB level) for auto-populated columns
  - Audit all code paths that construct the entity to ensure the field is always set

---

### 1.4 23514 — check_violation
- **Description:** An insert/update violated a `CHECK` constraint (a custom business rule enforced at the DB level).
- **Common Causes:**
  - Value outside allowed range/set (e.g., `status` not in the allowed enum list, negative `price`)
  - Application-level validation out of sync with the DB-level check definition
- **Symptoms:** Fails only for specific "invalid" values; passes application-level validation if that validation is looser than the DB check.
- **Example:**
```
ERROR: new row for relation "products" violates check constraint "chk_products_price_positive"
DETAIL: Failing row contains (55, Widget, -10.00).
```
- **Common Fixes:**
  - Align application-level validation rules exactly with DB check constraints to fail fast with a clearer message
  - Review the check constraint definition when adding new valid values (e.g., new enum/status values)

---

## 2. Class 42 — Syntax Error or Access Rule Violation

### 2.1 42P01 — undefined_table
- **Description:** A query references a table that doesn't exist (wrong name, wrong schema, or not yet migrated).
- **Common Causes:**
  - Typo in table name
  - Migration not yet applied to this environment
  - Wrong schema/search_path — table exists but in a different schema
- **Symptoms:** Fails consistently for a specific query/endpoint until fixed; often surfaces right after a deployment with pending migrations.
- **Example:**
```
ERROR: relation "orders_archive" does not exist
LINE 1: SELECT * FROM orders_archive WHERE...
```
- **Common Fixes:**
  - Verify migrations (Flyway/Liquibase) have run successfully in the target environment
  - Check `search_path` and fully qualify table names with schema (`schema.table`) if ambiguous
  - Confirm table name casing — Postgres folds unquoted identifiers to lowercase

---

### 2.2 42703 — undefined_column
- **Description:** A query references a column that doesn't exist on the specified table.
- **Common Causes:**
  - Typo in column name
  - Entity/JPA mapping out of sync with actual DB schema (e.g., renamed column, migration not applied)
  - Case-sensitivity issue from quoted identifiers created with mixed case
- **Symptoms:** Fails immediately on query execution; usually a clear signal of schema drift.
- **Example:**
```
ERROR: column "cutomer_id" does not exist
LINE 1: SELECT cutomer_id FROM orders;
HINT: Perhaps you meant to reference the column "orders.customer_id".
```
- **Common Fixes:**
  - Fix the typo (Postgres often suggests the correct column name in the HINT)
  - Ensure JPA entity `@Column` mappings match the actual DB schema after migrations
  - Run `\d table_name` in `psql` to confirm actual column names when debugging

---

### 2.3 42883 — undefined_function
- **Description:** A query calls a function/operator that doesn't exist for the given argument types.
- **Common Causes:**
  - Typo in function name
  - Calling a function with arguments of the wrong type (Postgres is strict about implicit casts in some contexts)
  - Extension providing the function (e.g., `pgcrypto`, `uuid-ossp`) not installed
- **Symptoms:** Fails on specific queries using that function; works fine in another environment that has the extension installed.
- **Example:**
```
ERROR: function gen_random_uuid() does not exist
HINT: No function matches the given name and argument types. You might need to add explicit type casts.
```
- **Common Fixes:**
  - Install the required extension: `CREATE EXTENSION IF NOT EXISTS pgcrypto;`
  - Add explicit type casts (`::text`, `::int`) when Postgres can't infer the intended overload
  - Verify the function exists in the target Postgres version (some functions are version-gated)

---

## 3. Class 40 — Transaction Rollback

### 3.1 40001 — serialization_failure
- **Description:** A transaction running at `SERIALIZABLE` (or sometimes `REPEATABLE READ`) isolation was rolled back because it could not be serialized with other concurrent transactions.
- **Common Causes:**
  - High write concurrency on overlapping rows under strict isolation levels
  - Long-running serializable transactions increasing the conflict window
- **Symptoms:** Intermittent failures under concurrent load; retrying the transaction typically succeeds.
- **Example:**
```
ERROR: could not serialize access due to concurrent update
```
  Surfaces in Java as `org.postgresql.util.PSQLException`, often translated to Spring's `DeadlockLoserDataAccessException` or a generic `TransactionSystemException`.
- **Common Fixes:**
  - Implement automatic retry logic for this specific SQLSTATE
  - Reconsider whether `SERIALIZABLE` isolation is truly needed vs. `READ COMMITTED` with explicit locking
  - Shorten transaction duration to reduce the conflict window

---

### 3.2 40P01 — deadlock_detected
- **Description:** Postgres detected a deadlock between two or more transactions and rolled back one of them.
- **Common Causes:**
  - Transactions acquiring locks on the same set of rows in different orders
  - Long transactions holding locks while waiting on application-level logic (e.g., external API calls) before committing
- **Symptoms:** Intermittent failures under concurrent write load; log shows the specific processes/transactions involved.
- **Example:**
```
ERROR: deadlock detected
DETAIL: Process 18421 waits for ShareLock on transaction 4291823; blocked by process 18455.
Process 18455 waits for ShareLock on transaction 4291810; blocked by process 18421.
HINT: See server log for query details.
```
- **Common Fixes:**
  - Standardize lock acquisition order across all transactions touching the same tables
  - Never perform slow external calls (network I/O, third-party APIs) while holding open a DB transaction
  - Add retry logic for this SQLSTATE specifically

---

### 3.3 25P02 — in_failed_sql_transaction
- **Description:** A statement was issued while the current transaction is already in a failed/aborted state from a prior error, and Postgres refuses further commands until rollback.
- **Common Causes:**
  - An earlier statement in the same transaction errored, but the application continued issuing more statements without rolling back
  - Missing error handling in batch/script execution that doesn't stop on first failure
- **Symptoms:** Confusing cascading errors — this error masks the real root cause, which occurred earlier in the same transaction.
- **Example:**
```
ERROR: current transaction is aborted, commands ignored until end of transaction block
```
- **Common Fixes:**
  - Look earlier in the transaction/log for the original error that caused the abort — this message itself isn't the real problem
  - Ensure the application rolls back (or uses savepoints) immediately after any statement failure
  - Use `SAVEPOINT` for operations where partial failure within a larger transaction should be recoverable

---

## 4. Class 08 — Connection Exceptions

### 4.1 08006 — connection_failure
- **Description:** The connection to the server was lost or could not be established after being previously active.
- **Common Causes:**
  - Network interruption between application and database
  - Database server restarted or crashed
  - Firewall/security group timing out idle connections
- **Symptoms:** In-flight queries fail; often affects multiple concurrent operations simultaneously.
- **Example:**
```
org.postgresql.util.PSQLException: An I/O error occurred while sending to the backend.
Caused by: java.net.SocketException: Connection reset
```
- **Common Fixes:**
  - Enable connection pool validation (`HikariCP` `connectionTestQuery` or `keepaliveTime`) to detect and evict stale connections
  - Check network stability and any intermediate proxies/load balancers with aggressive idle timeouts
  - Add TCP keepalive settings on the JDBC connection string

---

### 4.2 08003 — connection_does_not_exist
- **Description:** An operation was attempted on a connection that has already been closed.
- **Common Causes:**
  - Application code holds a reference to a connection/entity manager after it was closed elsewhere
  - Connection pool evicted the connection due to max lifetime settings while still in use
- **Symptoms:** Fails specifically on reused/stale connection references, often in long-lived objects or caches.
- **Example:**
```
org.postgresql.util.PSQLException: This connection has been closed.
```
- **Common Fixes:**
  - Never cache/store raw `Connection` or `EntityManager` objects beyond their intended scope
  - Align pool's `maxLifetime` with the database server's own connection timeout settings (pool value should be slightly shorter)

---

### 4.3 28P01 — invalid_password
- **Description:** Authentication failed due to an incorrect password for the specified role/user.
- **Common Causes:**
  - Wrong credentials in application configuration
  - Password rotated in the database but not updated in the app's secrets/config
- **Symptoms:** Fails at connection-pool startup or first connection attempt; consistent, not intermittent.
- **Example:**
```
FATAL: password authentication failed for user "app_user"
```
- **Common Fixes:**
  - Verify credentials in the secrets manager/config match the actual DB role's current password
  - Check for environment-specific config files accidentally pointing to the wrong environment's credentials

---

## 5. Class 53 — Insufficient Resources

### 5.1 53300 — too_many_connections
- **Description:** The database has reached its `max_connections` limit and refuses new connections.
- **Common Causes:**
  - Application connection pool sized larger than the DB can accommodate across all app instances
  - Connection leak (connections opened but never returned to the pool)
  - Multiple application instances/services all connecting to the same DB without a shared connection limit strategy
- **Symptoms:** New connection attempts fail while existing ones continue to work; often correlates with a recent scale-out event or leak buildup over time.
- **Example:**
```
FATAL: sorry, too many clients already
```
- **Common Fixes:**
  - Audit total connection pool sizes across all app instances vs. DB's `max_connections`
  - Use a connection pooler like PgBouncer in front of Postgres for high-instance-count deployments
  - Enable HikariCP leak detection (`leakDetectionThreshold`) to catch unreturned connections

---

### 5.2 53200 — out_of_memory
- **Description:** The Postgres server ran out of memory while executing a query or operation.
- **Common Causes:**
  - A single query (large sort, hash join, or aggregation) exceeding `work_mem` many times over
  - Too many concurrent connections each consuming their own `work_mem` allocation
- **Symptoms:** Specific heavy queries fail under load; sometimes correlates with server-wide memory pressure.
- **Example:**
```
ERROR: out of memory
DETAIL: Failed on request of size 8192 in memory context "ExecutorState".
```
- **Common Fixes:**
  - Tune `work_mem` conservatively considering `max_connections` (total possible usage = `work_mem` × concurrent operations)
  - Optimize the offending query (add indexes to avoid large sorts, reduce joined dataset size)
  - Consider `pg_stat_activity` and `EXPLAIN ANALYZE` to identify the memory-heavy query

---

## 6. Class 57 — Operator Intervention

### 6.1 57014 — query_canceled
- **Description:** A query was cancelled, most commonly due to hitting `statement_timeout`, but also possible from explicit `pg_cancel_backend()` or `pg_terminate_backend()`.
- **Common Causes:**
  - Query genuinely takes longer than the configured `statement_timeout`
  - Missing index causing a full table scan on a large table
  - Offset-based pagination at high offsets scanning and discarding many rows before returning results
- **Symptoms:** Fails only for "expensive" queries; simple queries on the same table succeed.
- **Example:**
```
ERROR: canceling statement due to statement timeout
```
  Surfaces in Java as `org.postgresql.util.PSQLException` → Spring's `QueryTimeoutException`.
- **Common Fixes:**
  - Run `EXPLAIN ANALYZE` to check for sequential scans that should be index scans
  - Replace offset-based pagination with keyset pagination (`WHERE id > :lastSeenId ORDER BY id LIMIT :pageSize`) for large tables — this keeps query cost roughly constant regardless of page depth
  - Only raise `statement_timeout` after confirming the query is genuinely optimized and the extra time is expected

---

### 6.2 55P03 — lock_not_available
- **Description:** A statement using `NOWAIT` (or hitting `lock_timeout`) could not acquire a required lock because another transaction already holds it.
- **Common Causes:**
  - Long-running transaction holding a row/table lock that a subsequent statement needs immediately
  - `SELECT ... FOR UPDATE NOWAIT` used in a high-contention path
- **Symptoms:** Fails immediately (rather than waiting) for a specific "hot" row/table under concurrent access.
- **Example:**
```
ERROR: could not obtain lock on row in relation "inventory"
```
- **Common Fixes:**
  - Reduce the duration of transactions holding locks on frequently-accessed rows
  - Decide whether the operation should wait (drop `NOWAIT`, set an appropriate `lock_timeout`) or fail fast with a retry strategy
  - Consider optimistic locking (`@Version`) instead of pessimistic locking for lower-contention alternatives

---

## 7. Class 22 — Data Exception

### 7.1 22001 — string_data_right_truncation
- **Description:** A value being inserted/updated is too long for the target column's defined length (e.g., `VARCHAR(50)`).
- **Common Causes:**
  - Application allows longer input than the DB column permits
  - Column size defined based on outdated requirements, now too small
- **Symptoms:** Fails only for longer-than-expected input values; short values succeed.
- **Example:**
```
ERROR: value too long for type character varying(50)
```
- **Common Fixes:**
  - Add matching `@Size(max = 50)` validation at the DTO layer so this fails with a clear message before reaching the DB
  - Reassess column size vs. actual business requirements; consider `TEXT` type if length is genuinely unbounded

---

### 7.2 22003 — numeric_value_out_of_range
- **Description:** A numeric value exceeds the range of its target column type (e.g., inserting a value too large for `INTEGER` or `SMALLINT`).
- **Common Causes:**
  - Column typed as `INTEGER` when values can exceed ~2.1 billion (should be `BIGINT`)
  - Application-side calculation producing an unexpectedly large number (e.g., overflow from a multiplication)
- **Symptoms:** Fails only when a specific large value is encountered — may work for months before triggering.
- **Example:**
```
ERROR: integer out of range
```
- **Common Fixes:**
  - Use `BIGINT` for columns that could realistically grow beyond `INTEGER` range (IDs, counters, monetary values in smallest unit)
  - Add range validation in application code for calculated values before persisting

---

## 8. Quick-Reference Decision Table

| Symptom | SQLSTATE | First Thing to Check |
|---|---|---|
| Insert fails, duplicate data | 23505 unique_violation | Unique constraint, race condition on pre-check |
| Insert/delete fails on related tables | 23503 foreign_key_violation | Referential state, `ON DELETE` behavior |
| Insert fails, missing required field | 23502 not_null_violation | DTO validation vs. DB schema |
| Query fails, table/column not found | 42P01 / 42703 | Migrations applied, schema/search_path, typos |
| Query fails only under concurrent load, retry works | 40001 / 40P01 | Lock ordering, isolation level, transaction duration |
| Cascading errors after one failure in a transaction | 25P02 in_failed_sql_transaction | The *earlier* error in the same transaction |
| New connections fail, existing ones fine | 53300 too_many_connections | Pool sizing vs. `max_connections`, connection leaks |
| Heavy query fails, simple ones succeed | 57014 query_canceled | `EXPLAIN ANALYZE`, pagination strategy, indexes |
| Long value insert fails | 22001 string_data_right_truncation | Column length vs. input validation |
| Large number insert fails | 22003 numeric_value_out_of_range | Column type (`INTEGER` vs `BIGINT`) |

---

*Document scope: PostgreSQL SQLSTATE error codes most commonly encountered from a Java/Spring/Hibernate backend, with their typical Java-side exception translation noted where relevant. Companion to `java_exceptions_reference.md` and `spring_exceptions_reference.md`. Intended for RAG retrieval.*
