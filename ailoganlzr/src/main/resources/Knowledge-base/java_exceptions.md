# Java Exceptions Reference Guide (RAG Knowledge Base)

This document catalogs Java exceptions and errors for retrieval-augmented generation use. Each entry follows a consistent schema: Type, Package, Description, Common Causes, Symptoms, Example Stack Trace, Common Fixes.

---

## 1. Checked Exceptions

### 1.1 IOException
- **Package:** `java.io`
- **Description:** Signals a failure during an I/O operation (file, stream, network).
- **Common Causes:**
  - Reading/writing to a closed stream
  - Disk full or file locked by another process
  - Network socket dropped mid-read
  - Invalid file path or missing file
- **Symptoms:** Application hangs or crashes during file/network read-write; partial file writes.
- **Example Stack Trace:**
```
java.io.IOException: Stream closed
    at java.base/java.io.BufferedInputStream.getBufIfOpen(BufferedInputStream.java:170)
    at java.base/java.io.BufferedInputStream.read(BufferedInputStream.java:344)
    at com.example.service.FileExportService.streamData(FileExportService.java:45)
    at com.example.controller.ExportController.download(ExportController.java:32)
```
- **Common Fixes:**
  - Use try-with-resources to manage stream lifecycle
  - Check `Files.exists()` before reading
  - Wrap and retry transient network I/O with backoff
  - Ensure streams aren't closed before async writes complete

---

### 1.2 FileNotFoundException
- **Package:** `java.io`
- **Description:** Subclass of `IOException`; thrown when a file with the specified pathname does not exist, or exists but is a directory/inaccessible.
- **Common Causes:**
  - Wrong relative/absolute path
  - File deleted between check and use (TOCTOU race)
  - Missing read/write permissions
- **Symptoms:** Immediate failure on `new FileInputStream(...)` or similar file-open calls.
- **Example Stack Trace:**
```
java.io.FileNotFoundException: /opt/app/config/settings.yaml (No such file or directory)
    at java.base/java.io.FileInputStream.open0(Native Method)
    at java.base/java.io.FileInputStream.open(FileInputStream.java:216)
    at java.base/java.io.FileInputStream.<init>(FileInputStream.java:157)
    at com.example.config.ConfigLoader.load(ConfigLoader.java:22)
```
- **Common Fixes:**
  - Use classpath-relative resource loading (`ClassLoader.getResourceAsStream`)
  - Validate path existence and permissions before opening
  - Bundle default config as a fallback resource

---

### 1.3 SQLException
- **Package:** `java.sql`
- **Description:** Thrown for database access errors — connectivity, syntax, constraint violations, timeouts.
- **Common Causes:**
  - Connection pool exhaustion
  - Invalid SQL syntax or wrong column/table name
  - Constraint violation (unique key, foreign key)
  - Connection timeout or DB server down
- **Symptoms:** API calls fail intermittently under load; connection pool metrics show exhaustion; queries hang then time out.
- **Example Stack Trace:**
```
java.sql.SQLException: Connection is not available, request timed out after 30000ms.
    at com.zaxxer.hikari.pool.HikariPool.createTimeoutException(HikariPool.java:695)
    at com.zaxxer.hikari.pool.HikariPool.getConnection(HikariPool.java:197)
    at com.example.repository.OrderRepository.findAll(OrderRepository.java:41)
```
- **Common Fixes:**
  - Tune connection pool size (HikariCP `maximumPoolSize`) to match DB capacity
  - Ensure connections are closed/released (try-with-resources or Spring-managed transactions)
  - Add query timeouts and retry logic for transient failures
  - Check for connection leaks with pool leak-detection settings

---

### 1.4 ClassNotFoundException
- **Package:** `java.lang`
- **Description:** Thrown when JVM tries to load a class by name via `Class.forName()`, `loadClass()`, or reflection, and no definition is found on the classpath.
- **Common Causes:**
  - Missing JAR dependency at runtime (present at compile time only)
  - Wrong classpath configuration in deployment
  - Typo in fully-qualified class name in config (e.g., JDBC driver name)
- **Symptoms:** App fails at startup or on first use of a specific feature (e.g., DB driver registration).
- **Example Stack Trace:**
```
java.lang.ClassNotFoundException: org.postgresql.Driver
    at java.base/jdk.internal.loader.BuiltinClassLoader.loadClass(BuiltinClassLoader.java:641)
    at java.base/jdk.internal.loader.ClassLoaders$AppClassLoader.loadClass(ClassLoaders.java:188)
    at java.base/java.lang.Class.forName0(Native Method)
    at java.base/java.lang.Class.forName(Class.java:467)
```
- **Common Fixes:**
  - Add missing dependency to build file (pom.xml / build.gradle)
  - Verify uber-jar/shaded jar packaging includes all dependencies
  - Check for classloader isolation issues in app servers

---

### 1.5 InterruptedException
- **Package:** `java.lang`
- **Description:** Thrown when a thread is interrupted while waiting, sleeping, or otherwise blocked (`Thread.sleep`, `Object.wait`, blocking queue operations).
- **Common Causes:**
  - Explicit `thread.interrupt()` call during shutdown
  - Executor service shutdown while tasks are running
  - Thread pool timeout cancellations
- **Symptoms:** Background tasks stop abruptly; graceful shutdown logs show interruption; thread pool tasks silently fail.
- **Example Stack Trace:**
```
java.lang.InterruptedException: sleep interrupted
    at java.base/java.lang.Thread.sleep(Native Method)
    at com.example.worker.PollingWorker.run(PollingWorker.java:67)
    at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1136)
```
- **Common Fixes:**
  - Re-set interrupt status: `Thread.currentThread().interrupt()` after catching
  - Never swallow silently — propagate or log with context
  - Design tasks to check `Thread.interrupted()` periodically for cooperative cancellation

---

### 1.6 ParseException
- **Package:** `java.text`
- **Description:** Thrown when text cannot be parsed into an expected format (dates, numbers).
- **Common Causes:**
  - Date/number string doesn't match expected pattern
  - Locale mismatch (e.g., comma vs period decimal separator)
- **Symptoms:** Fails on specific malformed input rows during batch import/export.
- **Example Stack Trace:**
```
java.text.ParseException: Unparseable date: "2024-13-45"
    at java.base/java.text.DateFormat.parse(DateFormat.java:395)
    at com.example.util.DateUtils.parseDate(DateUtils.java:18)
```
- **Common Fixes:**
  - Prefer `java.time` (`DateTimeFormatter`) over legacy `SimpleDateFormat`
  - Validate input format before parsing; provide clear error messages
  - Use strict `ResolverStyle.STRICT` to catch invalid dates like Feb 30

---

### 1.7 TimeoutException
- **Package:** `java.util.concurrent`
- **Description:** Thrown when a blocking operation (Future.get, latch await) exceeds its allotted time.
- **Common Causes:**
  - Downstream service slow or unresponsive
  - Insufficient timeout configured for expected workload
  - Deadlock or thread starvation preventing task completion
- **Symptoms:** Async tasks or futures never complete within SLA; cascading timeouts across services.
- **Example Stack Trace:**
```
java.util.concurrent.TimeoutException: null
    at java.base/java.util.concurrent.CompletableFuture.timedGet(CompletableFuture.java:1886)
    at java.base/java.util.concurrent.CompletableFuture.get(CompletableFuture.java:2021)
    at com.example.service.PaymentService.processAsync(PaymentService.java:55)
```
- **Common Fixes:**
  - Set realistic timeouts based on p99 latency of dependency
  - Add circuit breakers (Resilience4j) to fail fast
  - Investigate thread pool saturation as root cause

---

## 2. Unchecked Exceptions (RuntimeException subclasses)

### 2.1 NullPointerException (NPE)
- **Package:** `java.lang`
- **Description:** Thrown when code attempts to use a null reference where an object is required (method call, field access, array access).
- **Common Causes:**
  - Uninitialized field or Optional not checked
  - Method returns null instead of empty collection/Optional
  - Autoboxing null `Integer`/`Long` into primitive
  - Chained calls without null checks (`a.getB().getC()`)
- **Symptoms:** Random 500 errors on specific inputs; crash on a code path handling optional/missing data.
- **Example Stack Trace:**
```
java.lang.NullPointerException: Cannot invoke "String.trim()" because "email" is null
    at com.example.service.UserValidator.validate(UserValidator.java:29)
    at com.example.service.UserService.register(UserService.java:44)
```
- **Common Fixes:**
  - Use helpful NPE messages (enabled by default JDK 15+) to pinpoint the null variable
  - Use `Optional<T>` for values that may be absent
  - Validate/annotate with `@NonNull` and enable static analysis (SpotBugs, Error Prone)
  - Use `Objects.requireNonNull()` at API boundaries

---

### 2.2 ArrayIndexOutOfBoundsException
- **Package:** `java.lang`
- **Description:** Thrown when accessing an array with an illegal index (negative or ≥ length).
- **Common Causes:**
  - Off-by-one errors in loops
  - Assuming fixed array size that changed
  - Miscalculated index from user input
- **Symptoms:** Crash on edge-case input sizes (empty array, single element).
- **Example Stack Trace:**
```
java.lang.ArrayIndexOutOfBoundsException: Index 5 out of bounds for length 5
    at com.example.util.MatrixUtils.transpose(MatrixUtils.java:14)
```
- **Common Fixes:**
  - Use `<` not `<=` in loop bounds checks against `.length`
  - Prefer collections with bounds-safe accessors where possible
  - Add unit tests for boundary conditions (0, 1, max size)

---

### 2.3 StringIndexOutOfBoundsException
- **Package:** `java.lang`
- **Description:** Thrown by String methods (`charAt`, `substring`) when the index is out of range.
- **Common Causes:**
  - `substring(start, end)` with `end > length()`
  - Negative index from miscalculated offset
- **Symptoms:** Fails on strings shorter than expected (e.g., truncated input).
- **Example Stack Trace:**
```
java.lang.StringIndexOutOfBoundsException: begin 0, end 10, length 4
    at java.base/java.lang.String.checkBoundsBeginEnd(String.java:4602)
    at java.base/java.lang.String.substring(String.java:2704)
    at com.example.util.MaskingUtil.maskCard(MaskingUtil.java:12)
```
- **Common Fixes:**
  - Guard with `Math.min(desiredEnd, str.length())`
  - Validate string length before substring operations

---

### 2.4 IndexOutOfBoundsException
- **Package:** `java.lang`
- **Description:** General superclass for list/array index errors, thrown directly by `List.get()`, `List.remove()`, etc.
- **Common Causes:**
  - Iterating with a stale size after removals
  - Concurrent modification changing list size mid-loop
- **Symptoms:** Intermittent crashes in list-processing loops, especially under concurrent access.
- **Example Stack Trace:**
```
java.lang.IndexOutOfBoundsException: Index 3 out of bounds for length 3
    at java.base/java.util.Objects.checkIndex(Objects.java:385)
    at java.base/java.util.ArrayList.get(ArrayList.java:427)
    at com.example.service.BatchProcessor.processItem(BatchProcessor.java:33)
```
- **Common Fixes:**
  - Recompute `size()` inside loop conditions, or iterate over a copy
  - Use iterators with `remove()` instead of manual index tracking
  - Synchronize access for shared mutable lists

---

### 2.5 ClassCastException
- **Package:** `java.lang`
- **Description:** Thrown when code attempts an invalid downcast between incompatible types.
- **Common Causes:**
  - Unsafe cast after retrieving from a raw-typed or `Object`-typed collection
  - Deserialization returning an unexpected type
  - Generic type erasure hiding an incompatible cast at compile time
- **Symptoms:** Fails only for specific object types at runtime; compiles fine.
- **Example Stack Trace:**
```
java.lang.ClassCastException: class java.lang.Long cannot be cast to class java.lang.Integer
    at com.example.cache.CacheManager.getAs(CacheManager.java:27)
    at com.example.service.PricingService.getDiscount(PricingService.java:19)
```
- **Common Fixes:**
  - Use `instanceof` checks (or pattern matching `instanceof`) before casting
  - Avoid raw types; use proper generics end-to-end
  - Validate deserialized object types explicitly

---

### 2.6 ArithmeticException
- **Package:** `java.lang`
- **Description:** Thrown for illegal arithmetic operations, most commonly division by zero for integers.
- **Common Causes:**
  - Divisor computed from user input or aggregation that can be zero
  - Modulo by zero
- **Symptoms:** Crash on specific data combinations (e.g., empty dataset causing average = sum/0).
- **Example Stack Trace:**
```
java.lang.ArithmeticException: / by zero
    at com.example.analytics.StatsCalculator.average(StatsCalculator.java:21)
```
- **Common Fixes:**
  - Guard divisor with a zero-check before dividing
  - Use `BigDecimal` with explicit rounding/scale for financial math
  - Return a sentinel (0, NaN via double, or Optional) for undefined results

---

### 2.7 NumberFormatException
- **Package:** `java.lang`
- **Description:** Subclass of `IllegalArgumentException`; thrown when attempting to convert a string to a numeric type fails due to invalid format.
- **Common Causes:**
  - Parsing user input without validation (`Integer.parseInt("abc")`)
  - Locale-specific number formatting (commas, currency symbols) not stripped
  - Empty string or null passed to parse method
- **Symptoms:** Fails on malformed CSV/API input fields.
- **Example Stack Trace:**
```
java.lang.NumberFormatException: For input string: "12,345"
    at java.base/java.lang.NumberFormatException.forInputString(NumberFormatException.java:67)
    at java.base/java.lang.Integer.parseInt(Integer.java:668)
    at com.example.imports.CsvRowParser.parseQuantity(CsvRowParser.java:41)
```
- **Common Fixes:**
  - Strip formatting characters before parsing, or use `NumberFormat.parse()` with correct locale
  - Validate with regex before parsing
  - Wrap parsing in try-catch with row-level error reporting for batch imports

---

### 2.8 IllegalArgumentException
- **Package:** `java.lang`
- **Description:** Thrown to indicate a method has been passed an illegal or inappropriate argument.
- **Common Causes:**
  - Enum `valueOf()` called with invalid string
  - Negative value passed where only positive expected
  - Validation logic in constructors/setters rejecting bad input
- **Symptoms:** Fails fast at method entry with a clear validation message (when done well).
- **Example Stack Trace:**
```
java.lang.IllegalArgumentException: No enum constant com.example.model.OrderStatus.CANCELED
    at java.base/java.lang.Enum.valueOf(Enum.java:293)
    at com.example.model.OrderStatus.valueOf(OrderStatus.java:1)
    at com.example.mapper.OrderMapper.toStatus(OrderMapper.java:15)
```
- **Common Fixes:**
  - Check enum spelling consistency between client/server (`CANCELED` vs `CANCELLED`)
  - Validate inputs at API boundary with Bean Validation (`@NotNull`, `@Min`, custom validators)
  - Provide descriptive exception messages naming the invalid value and expected range

---

### 2.9 IllegalStateException
- **Package:** `java.lang`
- **Description:** Thrown when a method is invoked at an illegal or inappropriate time — object not in the right state for the operation.
- **Common Causes:**
  - Calling `next()` on an iterator without checking `hasNext()`
  - Reusing a one-shot object (e.g., a `Stream` after terminal operation)
  - Starting an already-started service/thread
- **Symptoms:** Fails intermittently depending on call order/lifecycle timing.
- **Example Stack Trace:**
```
java.lang.IllegalStateException: stream has already been operated upon or closed
    at java.base/java.util.stream.AbstractPipeline.evaluate(AbstractPipeline.java:246)
    at com.example.report.ExportService.buildRows(ExportService.java:38)
```
- **Common Fixes:**
  - Don't reuse consumed Streams — create a fresh stream per traversal
  - Add explicit state checks/guards before state-dependent operations
  - Use state machines or enums to track valid transitions

---

### 2.10 UnsupportedOperationException
- **Package:** `java.lang`
- **Description:** Thrown when a requested operation is not supported by the implementation, commonly on immutable collections.
- **Common Causes:**
  - Calling `.add()`/`.remove()` on `List.of(...)`, `Arrays.asList(...)`, or `Collections.unmodifiableList(...)`
  - Interface method intentionally left unimplemented
- **Symptoms:** Fails only when code tries to mutate a collection that looked mutable.
- **Example Stack Trace:**
```
java.lang.UnsupportedOperationException: null
    at java.base/java.util.ImmutableCollections.uoe(ImmutableCollections.java:142)
    at java.base/java.util.ImmutableCollections$AbstractImmutableList.add(ImmutableCollections.java:258)
    at com.example.service.TagService.addTag(TagService.java:20)
```
- **Common Fixes:**
  - Wrap in `new ArrayList<>(originalList)` if mutation is needed
  - Be explicit about which factory methods return immutable collections
  - Document mutability expectations in method Javadoc

---

### 2.11 ConcurrentModificationException
- **Package:** `java.util`
- **Description:** Thrown when a collection is structurally modified while being iterated, other than through the iterator's own methods.
- **Common Causes:**
  - Calling `list.remove(item)` inside a for-each loop over `list`
  - Multiple threads modifying a non-thread-safe collection concurrently
- **Symptoms:** Fails unpredictably, often only under specific data conditions or concurrency timing.
- **Example Stack Trace:**
```
java.util.ConcurrentModificationException: null
    at java.base/java.util.ArrayList$Itr.checkForComodification(ArrayList.java:1013)
    at java.base/java.util.ArrayList$Itr.next(ArrayList.java:967)
    at com.example.service.InventoryService.pruneExpired(InventoryService.java:26)
```
- **Common Fixes:**
  - Use `Iterator.remove()` instead of collection `remove()` during iteration
  - Use `CopyOnWriteArrayList` or `ConcurrentHashMap` for concurrent access patterns
  - Collect items to remove in a separate list, then remove after the loop

---

### 2.12 NoSuchElementException
- **Package:** `java.util`
- **Description:** Thrown by iterators (`next()`) or `Optional.get()` when no element/value is present.
- **Common Causes:**
  - Calling `iterator.next()` without checking `hasNext()`
  - Calling `Optional.get()` on an empty Optional without `isPresent()` check
- **Symptoms:** Crashes on empty collections/results where a value was assumed present.
- **Example Stack Trace:**
```
java.util.NoSuchElementException: No value present
    at java.base/java.util.Optional.get(Optional.java:143)
    at com.example.repository.UserRepository.getActiveUser(UserRepository.java:31)
```
- **Common Fixes:**
  - Use `Optional.orElseThrow(customException)` for clearer error semantics
  - Always check `hasNext()`/`isPresent()` before `next()`/`get()`
  - Prefer `orElse()`/`orElseGet()` for safe defaults

---

### 2.13 NegativeArraySizeException
- **Package:** `java.lang`
- **Description:** Thrown when code attempts to create an array with a negative size.
- **Common Causes:**
  - Size computed from subtraction that goes negative
  - Untrusted input directly used as array length
- **Symptoms:** Crashes during array allocation with unusual/malicious input.
- **Example Stack Trace:**
```
java.lang.NegativeArraySizeException: -1
    at com.example.util.BufferUtils.allocate(BufferUtils.java:9)
```
- **Common Fixes:**
  - Validate computed sizes are ≥ 0 before allocation
  - Use collections (`ArrayList`) instead of raw arrays when size is dynamic

---

### 2.14 ArrayStoreException
- **Package:** `java.lang`
- **Description:** Thrown when an attempt is made to store an incompatible type into an array of objects.
- **Common Causes:**
  - Assigning an `Object[]` reference actually backed by a `String[]`, then storing an `Integer` into it
- **Symptoms:** Runtime failure despite passing compile-time type checks (due to array covariance).
- **Example Stack Trace:**
```
java.lang.ArrayStoreException: java.lang.Integer
    at com.example.util.ArrayHelper.fill(ArrayHelper.java:11)
```
- **Common Fixes:**
  - Prefer generic collections (`List<T>`) over arrays for heterogeneous-risk scenarios
  - Avoid unchecked array covariance patterns

---

### 2.15 IllegalMonitorStateException
- **Package:** `java.lang`
- **Description:** Thrown when a thread calls `wait()`, `notify()`, or `notifyAll()` on an object without owning that object's monitor (lock).
- **Common Causes:**
  - Calling `notify()`/`wait()` outside a `synchronized` block on the same object
- **Symptoms:** Crash in concurrent producer/consumer code, often intermittent.
- **Example Stack Trace:**
```
java.lang.IllegalMonitorStateException: current thread is not owner
    at java.base/java.lang.Object.notifyAll(Native Method)
    at com.example.queue.BlockingTaskQueue.push(BlockingTaskQueue.java:24)
```
- **Common Fixes:**
  - Always call `wait()`/`notify()` inside `synchronized(lock)` blocks on the same monitor object
  - Prefer higher-level concurrency utilities (`java.util.concurrent` locks, `BlockingQueue`) over raw wait/notify

---

### 2.16 RejectedExecutionException
- **Package:** `java.util.concurrent`
- **Description:** Thrown by an `Executor` when a task cannot be accepted for execution (pool shut down or queue saturated).
- **Common Causes:**
  - Submitting tasks after `executor.shutdown()` called
  - Bounded queue full and rejection policy is `AbortPolicy` (default)
- **Symptoms:** Tasks fail immediately under high load or during shutdown sequences.
- **Example Stack Trace:**
```
java.util.concurrent.RejectedExecutionException: Task com.example.job.ReportJob@3af rejected from java.util.concurrent.ThreadPoolExecutor@1e[Running, pool size = 10, active threads = 10, queued tasks = 100, completed tasks = 542]
    at java.base/java.util.concurrent.ThreadPoolExecutor$AbortPolicy.rejectedExecution(ThreadPoolExecutor.java:2065)
    at java.base/java.util.concurrent.ThreadPoolExecutor.reject(ThreadPoolExecutor.java:833)
    at com.example.async.JobDispatcher.submit(JobDispatcher.java:17)
```
- **Common Fixes:**
  - Size thread pool and queue capacity to expected throughput
  - Use a `CallerRunsPolicy` or custom backpressure strategy instead of aborting
  - Check for shutdown state before submitting new tasks

---

## 3. Errors (java.lang.Error subclasses — generally not meant to be caught)

### 3.1 OutOfMemoryError
- **Package:** `java.lang`
- **Description:** Thrown when the JVM cannot allocate an object because it has run out of heap (or other memory area) space.
- **Common Causes:**
  - Memory leak (objects retained via static collections, caches without eviction, unclosed resources)
  - Loading an entire large dataset into memory (e.g., `List<>` of 30k+ entities instead of streaming)
  - Heap size (`-Xmx`) too small for workload
  - Metaspace exhaustion from excessive classloading (common in hot-redeploy scenarios)
- **Symptoms:** App crashes or becomes unresponsive under load; GC pauses increase before the crash; heap dump shows retained objects growing over time.
- **Example Stack Trace:**
```
java.lang.OutOfMemoryError: Java heap space
    at java.base/java.util.Arrays.copyOf(Arrays.java:3745)
    at java.base/java.util.ArrayList.grow(ArrayList.java:237)
    at com.example.export.ReportExportService.loadAllRecords(ReportExportService.java:52)
    at com.example.controller.ExportController.export(ExportController.java:29)
```
- **Common Fixes:**
  - Stream large result sets instead of materializing full lists (JPA `Stream<>`, cursor-based pagination)
  - Take and analyze a heap dump (`jmap`, Eclipse MAT) to find retained-object roots
  - Tune `-Xmx`/`-Xms` appropriately; consider G1GC or ZGC for large heaps
  - Add bounded caches with eviction (Caffeine, Guava Cache) instead of unbounded maps

---

### 3.2 StackOverflowError
- **Package:** `java.lang`
- **Description:** Thrown when a thread's call stack exceeds its maximum depth, typically from deep or infinite recursion.
- **Common Causes:**
  - Missing or incorrect base case in recursive method
  - Mutually recursive calls forming an infinite loop
  - Deeply nested object graphs (e.g., circular references in `toString()`/serialization)
- **Symptoms:** Crash after a burst of very deep, repetitive stack frames in the trace.
- **Example Stack Trace:**
```
java.lang.StackOverflowError: null
    at com.example.model.Category.getFullPath(Category.java:18)
    at com.example.model.Category.getFullPath(Category.java:18)
    at com.example.model.Category.getFullPath(Category.java:18)
    ... (repeated thousands of times)
```
- **Common Fixes:**
  - Verify and fix recursion base cases
  - Convert deep recursion to iterative approaches with an explicit stack
  - Break circular references (e.g., exclude back-references in `toString`/`equals`/JSON serialization with `@JsonManagedReference`/`@JsonBackReference`)
  - Increase `-Xss` thread stack size only as a last resort, not a fix for a bug

---

### 3.3 NoClassDefFoundError
- **Package:** `java.lang`
- **Description:** Thrown when the JVM cannot find a class definition that was available at compile time but is missing at runtime (different from `ClassNotFoundException`, which is for dynamic loading).
- **Common Causes:**
  - Class initialization failed earlier (a static initializer threw an exception)
  - Dependency JAR missing or excluded from the final deployment artifact
  - Version mismatch between compile-time and runtime dependency
- **Symptoms:** App fails at startup or on first access to a class, even though it compiled successfully.
- **Example Stack Trace:**
```
java.lang.NoClassDefFoundError: org/springframework/data/domain/Pageable
    at com.example.repository.UserRepository.findAll(UserRepository.java:10)
Caused by: java.lang.ClassNotFoundException: org.springframework.data.domain.Pageable
    at java.base/jdk.internal.loader.BuiltinClassLoader.loadClass(BuiltinClassLoader.java:641)
```
- **Common Fixes:**
  - Check for dependency version conflicts (`mvn dependency:tree` / `gradle dependencies`)
  - Ensure the failing class's static initializer isn't throwing (check "Caused by" chain)
  - Rebuild and redeploy full artifact to avoid partial/stale JARs

---

### 3.4 ExceptionInInitializerError
- **Package:** `java.lang`
- **Description:** Thrown when an exception occurs during evaluation of a static initializer or static field initialization.
- **Common Causes:**
  - Static block throwing an unchecked exception (e.g., loading a config file that doesn't exist)
  - Static final field initialization dividing by zero or NPE
- **Symptoms:** First reference to the class fails; subsequent references throw `NoClassDefFoundError` (class marked erroneous).
- **Example Stack Trace:**
```
java.lang.ExceptionInInitializerError
    at com.example.config.AppConstants.<clinit>(AppConstants.java:14)
Caused by: java.lang.NumberFormatException: For input string: ""
    at java.base/java.lang.Integer.parseInt(Integer.java:668)
    at com.example.config.AppConstants.<clinit>(AppConstants.java:12)
```
- **Common Fixes:**
  - Avoid risky operations (I/O, parsing) in static initializers; use lazy initialization instead
  - Wrap static init logic in try-catch with clear fallback/logging
  - Move environment-dependent config loading to a Spring `@PostConstruct` or bean factory method

---

### 3.5 AssertionError
- **Package:** `java.lang`
- **Description:** Thrown by the `assert` statement when the asserted condition is false (only active with `-ea` JVM flag).
- **Common Causes:**
  - Invariant violated during development/testing
  - Defensive assertions on "impossible" code paths (e.g., default case in switch)
- **Symptoms:** Only appears when assertions are enabled (tests, some dev environments); silent in production by default.
- **Example Stack Trace:**
```
java.lang.AssertionError: Order total must be non-negative
    at com.example.model.Order.validate(Order.java:33)
```
- **Common Fixes:**
  - Don't rely on assertions for production validation — use explicit exceptions instead
  - Reserve `assert` for internal invariants checked during testing/development

---

### 3.6 NoSuchMethodError
- **Package:** `java.lang`
- **Description:** Thrown when code tries to call a method that doesn't exist in the loaded version of a class — a binary incompatibility, typically from version mismatch.
- **Common Causes:**
  - Two different versions of the same library on the classpath (dependency conflict)
  - Compiled against a newer library version than what's deployed at runtime
- **Symptoms:** Fails only in certain environments (works on dev machine, fails in prod) due to differing classpath.
- **Example Stack Trace:**
```
java.lang.NoSuchMethodError: 'java.lang.String org.apache.commons.lang3.StringUtils.abbreviate(java.lang.String, int, int)'
    at com.example.util.TextUtils.truncate(TextUtils.java:8)
```
- **Common Fixes:**
  - Resolve dependency version conflicts explicitly (dependency management / BOM)
  - Use `mvn dependency:tree -Dverbose` to spot duplicate artifact versions
  - Align library versions across all modules/services

---

## 4. Spring / Hibernate / JPA-Specific Exceptions

### 4.1 LazyInitializationException
- **Package:** `org.hibernate`
- **Description:** Thrown when code tries to access a lazily-loaded association/collection after the owning Hibernate `Session` has been closed.
- **Common Causes:**
  - Accessing a lazy `@OneToMany`/`@ManyToOne` field in a controller/view layer after the `@Transactional` service method has returned
  - Serializing an entity to JSON outside the transaction boundary
- **Symptoms:** Works fine inside service layer; fails specifically when serializing/rendering the response.
- **Example Stack Trace:**
```
org.hibernate.LazyInitializationException: failed to lazily initialize a collection of role: com.example.model.Order.items, could not initialize proxy - no Session
    at org.hibernate.collection.internal.AbstractPersistentCollection.throwLazyInitializationException(AbstractPersistentCollection.java:602)
    at com.example.controller.OrderController.getOrder(OrderController.java:24)
```
- **Common Fixes:**
  - Use `JOIN FETCH` in JPQL/HQL to eagerly fetch needed associations
  - Use DTO projections instead of returning entities directly
  - Enable `Open Session in View` only as a stopgap (has its own tradeoffs) — prefer explicit fetch strategies

---

### 4.2 OptimisticLockException
- **Package:** `jakarta.persistence` (formerly `javax.persistence`)
- **Description:** Thrown when optimistic concurrency control (`@Version` field) detects the entity was modified by another transaction since it was read.
- **Common Causes:**
  - Two concurrent requests updating the same row
  - Stale entity held in memory/cache for too long before update
- **Symptoms:** Intermittent failures under concurrent writes to the same record; more frequent under high contention.
- **Example Stack Trace:**
```
jakarta.persistence.OptimisticLockException: Row was updated or deleted by another transaction
    at org.hibernate.internal.ExceptionConverterImpl.convert(ExceptionConverterImpl.java:186)
    at com.example.service.InventoryService.updateStock(InventoryService.java:40)
```
- **Common Fixes:**
  - Catch and retry with fresh entity reload on conflict
  - Reduce transaction hold time to minimize contention window
  - Consider pessimistic locking (`@Lock(LockModeType.PESSIMISTIC_WRITE)`) for high-contention hotspots

---

### 4.3 ConstraintViolationException (Hibernate / Bean Validation)
- **Package:** `org.hibernate.exception` or `jakarta.validation`
- **Description:** Two distinct common exceptions share this name — one from Hibernate (DB constraint violation on flush), one from Bean Validation (`@Valid` failures). Both indicate a data rule was violated.
- **Common Causes:**
  - Unique constraint violated (duplicate email/username)
  - `@NotNull`/`@Size`/`@Pattern` validation failing on a DTO
  - Foreign key violation (referencing a non-existent parent row)
- **Symptoms:** Fails on save/insert with specific data; validation errors surface at controller boundary.
- **Example Stack Trace:**
```
org.hibernate.exception.ConstraintViolationException: could not execute statement
    at org.hibernate.exception.internal.SQLStateConversionDelegate.convert(SQLStateConversionDelegate.java:97)
Caused by: java.sql.SQLIntegrityConstraintViolationException: Duplicate entry 'jane@example.com' for key 'users.uk_email'
```
- **Common Fixes:**
  - Add pre-save uniqueness checks or handle the DB exception with a friendly error message
  - Apply Bean Validation annotations at the DTO layer for early rejection
  - Use `@ControllerAdvice`/`@ExceptionHandler` to map to clean 4xx API responses

---

### 4.4 DataIntegrityViolationException
- **Package:** `org.springframework.dao`
- **Description:** Spring's translated, unchecked wrapper around low-level DB integrity errors (unique/foreign key/not-null violations), part of Spring's consistent `DataAccessException` hierarchy.
- **Common Causes:**
  - Same root causes as constraint violations above, but surfaced through Spring Data repositories
- **Symptoms:** Thrown from `repository.save()` calls; wraps the underlying `SQLException`.
- **Example Stack Trace:**
```
org.springframework.dao.DataIntegrityViolationException: could not execute statement; SQL [n/a]; constraint [uk_email]
    at org.springframework.orm.jpa.vendor.HibernateJpaDialect.convertHibernateAccessException(HibernateJpaDialect.java:271)
    at com.example.repository.UserRepository.save(UserRepository.java:1)
```
- **Common Fixes:**
  - Catch this Spring-specific exception in service layer instead of vendor-specific SQL exceptions (keeps code DB-agnostic)
  - Validate uniqueness/business rules before attempting the save
  - Provide user-friendly error translation in a global exception handler

---

### 4.5 TransactionRequiredException
- **Package:** `jakarta.persistence`
- **Description:** Thrown when an operation requiring an active transaction (e.g., `EntityManager.persist()`) is called outside of one.
- **Common Causes:**
  - Missing `@Transactional` annotation on a service method that writes to the DB
  - Calling a transactional method from within the same class (self-invocation bypasses Spring's proxy)
- **Symptoms:** Fails specifically on write operations; reads may work fine.
- **Example Stack Trace:**
```
jakarta.persistence.TransactionRequiredException: Executing an update/delete query
    at org.hibernate.internal.AbstractSharedSessionContract.checkTransactionNeededForUpdateOperation(AbstractSharedSessionContract.java:495)
    at com.example.repository.CustomOrderRepositoryImpl.bulkUpdateStatus(CustomOrderRepositoryImpl.java:22)
```
- **Common Fixes:**
  - Add `@Transactional` to the enclosing service method
  - Avoid self-invocation of `@Transactional` methods within the same class (Spring AOP proxies won't intercept it) — refactor into a separate bean
  - Verify transaction manager is correctly configured for the persistence unit

---

### 4.6 EntityNotFoundException
- **Package:** `jakarta.persistence`
- **Description:** Thrown when `EntityManager.getReference()` is used and the referenced entity doesn't actually exist in the database, or when a lazy proxy is accessed for a deleted row.
- **Common Causes:**
  - Referencing an entity by ID that was deleted by another process
  - Using `getReference()`/`getOne()` instead of `findById()` and not handling the "not found" case
- **Symptoms:** Fails when the proxy is first accessed, not at lookup time — can surface far from the actual root cause.
- **Example Stack Trace:**
```
jakarta.persistence.EntityNotFoundException: Unable to find com.example.model.Customer with id 10423
    at org.hibernate.internal.SessionFactoryImpl$SessionBuilderImpl.openSession(SessionFactoryImpl.java:775)
    at com.example.service.OrderService.attachCustomer(OrderService.java:29)
```
- **Common Fixes:**
  - Prefer `findById()` returning `Optional<T>` with explicit `orElseThrow()` over `getReference()`
  - Handle deleted-parent-row scenarios explicitly with clear custom exceptions
  - Add referential integrity checks before establishing associations

---

## 5. Networking Exceptions

### 5.1 SocketTimeoutException
- **Package:** `java.net`
- **Description:** Thrown when a socket read or accept operation times out.
- **Common Causes:**
  - Downstream service slow to respond
  - Timeout configured too aggressively for expected latency
  - Network congestion or packet loss
- **Symptoms:** Intermittent failures calling external APIs, worse under load or network instability.
- **Example Stack Trace:**
```
java.net.SocketTimeoutException: Read timed out
    at java.base/java.net.SocketInputStream.socketRead0(Native Method)
    at java.base/java.net.SocketInputStream.read(SocketInputStream.java:168)
    at com.example.client.PaymentGatewayClient.charge(PaymentGatewayClient.java:38)
```
- **Common Fixes:**
  - Tune connect/read timeouts based on measured p99 latency
  - Implement retry with exponential backoff for idempotent calls
  - Add circuit breaker to avoid cascading failures

---

### 5.2 ConnectException
- **Package:** `java.net`
- **Description:** Thrown when a connection is refused by the remote host (e.g., nothing listening on that port).
- **Common Causes:**
  - Target service is down or not yet started
  - Wrong host/port configuration
  - Firewall blocking the connection
- **Symptoms:** Fails immediately (fast failure) rather than hanging, distinguishing it from timeout scenarios.
- **Example Stack Trace:**
```
java.net.ConnectException: Connection refused
    at java.base/sun.nio.ch.Net.pollConnect(Native Method)
    at java.base/sun.nio.ch.Net.pollConnectNow(Net.java:672)
    at com.example.client.InventoryServiceClient.checkStock(InventoryServiceClient.java:15)
```
- **Common Fixes:**
  - Verify service discovery/config points to the correct host and port
  - Check target service health and startup order in deployment
  - Add readiness probes/health checks before routing traffic

---

### 5.3 UnknownHostException
- **Package:** `java.net`
- **Description:** Thrown when DNS resolution fails for a given hostname.
- **Common Causes:**
  - Typo in hostname/URL configuration
  - DNS server unreachable or misconfigured
  - Service not yet registered in service discovery
- **Symptoms:** Fails immediately on any attempt to open a connection to the host.
- **Example Stack Trace:**
```
java.net.UnknownHostException: api.exmaple-typo.com
    at java.base/java.net.InetAddress$CachedAddresses.get(InetAddress.java:797)
    at java.base/java.net.InetAddress.getAllByName0(InetAddress.java:1519)
    at com.example.client.ExternalApiClient.call(ExternalApiClient.java:20)
```
- **Common Fixes:**
  - Double-check hostname spelling in configuration
  - Verify DNS resolution works from the deployment environment (`nslookup`/`dig`)
  - Externalize hostnames to config to avoid hardcoded typos

---

### 5.4 SSLHandshakeException
- **Package:** `javax.net.ssl`
- **Description:** Thrown when the SSL/TLS handshake fails between client and server.
- **Common Causes:**
  - Expired or untrusted certificate
  - Missing intermediate certificate in the chain
  - Client truststore doesn't include the server's CA
  - Protocol/cipher suite mismatch
- **Symptoms:** Fails only for HTTPS connections to a specific host; often after a certificate renewal or truststore change.
- **Example Stack Trace:**
```
javax.net.ssl.SSLHandshakeException: PKIX path building failed: unable to find valid certification path to requested target
    at java.base/sun.security.ssl.Alert.createSSLException(Alert.java:131)
    at java.base/sun.security.ssl.TransportContext.fatal(TransportContext.java:361)
    at com.example.client.SecureApiClient.call(SecureApiClient.java:44)
```
- **Common Fixes:**
  - Import the server's CA/intermediate certs into the JVM truststore (`keytool -importcert`)
  - Check certificate expiry and renewal automation
  - Verify TLS protocol versions are compatible on both ends

---

## 6. Quick-Reference Decision Table

| Symptom | Likely Exception | First Thing to Check |
|---|---|---|
| App crashes under sustained load, GC pauses grow | OutOfMemoryError | Heap dump, streaming vs. full-list loading |
| Deep recursive call, repeated identical frames | StackOverflowError | Recursion base case |
| Works in dev, fails in prod on class access | NoClassDefFoundError / NoSuchMethodError | Dependency version conflicts |
| Random NPE on chained getters | NullPointerException | Null checks / Optional usage |
| Fails only on entity access after controller returns | LazyInitializationException | Fetch strategy / DTO projection |
| Concurrent writes to same row failing intermittently | OptimisticLockException | Retry logic / lock contention |
| Fails immediately connecting to external service | ConnectException | Host/port config, service health |
| Fails after long wait connecting to external service | SocketTimeoutException | Timeout tuning, downstream latency |
| Bulk import fails on specific rows only | NumberFormatException / ParseException | Input validation, locale handling |
| List processing fails intermittently under concurrency | ConcurrentModificationException | Iterator-safe removal, concurrent collections |

---

*Document scope: JVM standard library exceptions/errors, plus common Spring/Hibernate/JPA and networking exceptions relevant to backend Java development. Intended for RAG retrieval — each section is self-contained and chunk-friendly.*
