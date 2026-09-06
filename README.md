# AILogAnalyser

AILogAnalyser is a Java/Spring Boot based exception analysis system that helps developers understand application failures by combining **stack traces, relevant application source code, retrieved knowledge, and AI-generated analysis**.

Instead of sending only a raw exception message to an LLM, AILogAnalyser attempts to provide the model with additional context about **where the exception occurred and what the surrounding application code is doing**.

The project is split into a reusable **client library** and a remotely deployable **analysis server**.

---

## Why AILogAnalyser?

A normal Java stack trace tells us things such as:

- which exception occurred,
- the exception message,
- classes and methods involved,
- and the sequence of calls leading to the failure.

However, understanding the actual cause often requires manually opening those classes, navigating through methods, understanding the execution flow, and correlating the exception with application logic.

AILogAnalyser is an experiment in automating part of that debugging workflow.

Instead of analysing only:

```text
java.lang.NullPointerException
    at com.example.service.OrderService.processOrder(OrderService.java:64)
```

the analyser can additionally extract relevant source-code context from the host application, retrieve related knowledge from a vector store, and construct a richer prompt for the AI model.

---

# Architecture

AILogAnalyser follows a **client-server architecture**.

```text
┌──────────────────────────────────────┐
│         Host Spring Boot App         │
│                                      │
│   GlobalExceptionHandler             │
│              │                       │
│              ▼                       │
│      ailoganlzr-client               │
│                                      │
│  • captures exception information    │
│  • extracts stack trace              │
│  • locates relevant source code      │
│  • builds analysis request           │
└──────────────────┬───────────────────┘
                   │
                   │ HTTP
                   ▼
┌──────────────────────────────────────┐
│          AILogAnalyser Server        │
│                                      │
│          Analysis Service            │
│             │        │               │
│             │        └──── Redis     │
│             │              Cache     │
│             ▼                        │
│          RAG Service                 │
│             │                        │
│             ▼                        │
│      PostgreSQL + PGVector           │
│             │                        │
│             ▼                        │
│      Relevant Knowledge              │
│             │                        │
│             ▼                        │
│        Prompt Builder                │
│             │                        │
│             ▼                        │
│        Gemini via Spring AI          │
│             │                        │
│             ▼                        │
│     Structured AI Analysis           │
│             │                        │
│             ▼                        │
│          PostgreSQL                  │
└──────────────────────────────────────┘
```

---

# How It Works

## 1. An exception occurs in the host application

AILogAnalyser is intended to be integrated with a Spring Boot application's exception-handling flow.

When an exception reaches the application's global exception handler, the AILogAnalyser client can build an analysis request from that exception.

The host application continues to own its exception handling. AILogAnalyser acts as an additional analysis layer.

---

## 2. The client analyses the stack trace

The client examines the stack trace and identifies application-level stack frames.

A stack frame such as:

```text
at com.example.service.OrderService.processOrder(OrderService.java:64)
```

provides information such as:

```text
Class  : com.example.service.OrderService
Method : processOrder
File   : OrderService.java
Line   : 64
```

This information is used to locate the corresponding source code in the host application.

---

## 3. Relevant source code is extracted

AILogAnalyser does not rely only on the raw stack trace.

The client contains source-code extraction logic that uses **JavaParser** to inspect Java source files and identify relevant methods.

This allows the analysis request to contain context about the code associated with the exception rather than providing the AI model with only an exception string.

The source root can be explicitly configured when necessary. The client also contains source-root resolution logic so that the application location does not need to be permanently hard-coded to one developer's machine.

---

## 4. The request is sent to the AILogAnalyser server

The client communicates with the analysis server over HTTP.

This separation means the host application does **not** contain the AI, vector database, persistence, or caching implementation.

```text
Host Application
       │
       │ ailoganlzr-client
       ▼
AILogAnalyser Server
```

Multiple applications can therefore integrate the client while the analysis logic remains centralized in the server.

---

## 5. Redis cache is checked

AILogAnalyser uses **Redis** to cache analysis responses.

The cache helps avoid unnecessarily repeating an AI analysis for an already cached request.

Conceptually:

```text
Analysis Request
       │
       ▼
   Check Redis
     /     \
   HIT     MISS
    │        │
    │        ▼
    │    AI Analysis
    │        │
    │        ▼
    │    Store Cache
    │        │
    └────────┘
         │
         ▼
      Response
```

---

## 6. Relevant knowledge is retrieved using RAG

The server contains a Retrieval-Augmented Generation (RAG) pipeline.

Knowledge documents are loaded, split into smaller chunks and stored as vector embeddings in **PGVector**.

For an incoming stack trace, AILogAnalyser performs a similarity search against the vector store and retrieves the most relevant documents.

The current retrieval configuration uses:

```text
Top K = 3
```

The purpose of this stage is to provide the model with relevant debugging knowledge in addition to the exception itself.

---

## 7. A context-rich prompt is constructed

The prompt supplied to the AI model combines multiple sources of information:

```text
Raw Stack Trace
       +
Relevant Source Code / Execution Context
       +
Knowledge Retrieved through RAG
       ↓
Structured Prompt
       ↓
Gemini
```

This is one of the central ideas behind the project.

The AI is not expected to diagnose the failure from an isolated error message when more useful application context can be supplied.

---

## 8. Gemini generates the analysis

AILogAnalyser integrates with **Google Gemini through Spring AI**.

Spring AI's `ChatClient` is used to communicate with the model.

The response is converted into the application's structured AI analysis response rather than being treated purely as arbitrary text.

---

## 9. Analysis information is persisted

AILogAnalyser uses **PostgreSQL** for persistent application data.

The server maintains analysis request/result information through Spring Data JPA repositories.

PGVector is also backed by PostgreSQL, allowing the project to use PostgreSQL both for normal relational persistence and vector-based similarity search.

---

# Project Structure

AILogAnalyser is a Maven multi-module project.

```text
AILogAnalyser/
│
├── pom.xml
│
├── ailoganlzr-client/
│   ├── pom.xml
│   └── src/
│
└── ailoganlzr-server/
    ├── pom.xml
    └── src/
```

### `ailoganlzr-client`

Reusable library that is added to another Spring Boot application.

Its responsibilities include:

- integration with the host application's exception flow,
- stack-trace processing,
- application source-code extraction,
- building the analysis request,
- and communicating with the remote AILogAnalyser server.

### `ailoganlzr-server`

The central analysis service.

Its responsibilities include:

- receiving analysis requests,
- caching,
- persistence,
- RAG,
- vector similarity search,
- prompt construction,
- Gemini communication,
- and returning the analysis response.

---

# Technology Stack

| Area | Technology |
|---|---|
| Language | Java |
| Backend | Spring Boot |
| AI Integration | Spring AI |
| LLM | Google Gemini |
| Database | PostgreSQL |
| Vector Store | PGVector |
| Cache | Redis |
| Persistence | Spring Data JPA / Hibernate |
| Source Analysis | JavaParser |
| Metrics | Micrometer |
| Build | Maven |
| Containerization | Docker |
| Server Hosting | Render |
| Client Distribution | GitHub Packages |

---

# Integrating AILogAnalyser Into Another Application

The client is published as a Maven package through **GitHub Packages**.

Current artifact:

```text
Group ID    : com.proj1
Artifact ID : ailoganlzr-client
Version     : 0.0.1-SNAPSHOT
```

> GitHub Packages requires authentication for Maven package downloads. Consumers therefore need their own GitHub Personal Access Token with `read:packages` permission.

## 1. Configure Maven authentication

Add GitHub Packages credentials to:

```text
~/.m2/settings.xml
```

Example:

```xml
<settings>
    <servers>
        <server>
            <id>github</id>
            <username>YOUR_GITHUB_USERNAME</username>
            <password>YOUR_GITHUB_PAT</password>
        </server>
    </servers>
</settings>
```

Do not commit the Personal Access Token to source control.

---

## 2. Add the GitHub Packages repository

Add the repository to the host application's `pom.xml`:

```xml
<repositories>
    <repository>
        <id>github</id>
        <name>GitHub AILogAnalyser Packages</name>
        <url>https://maven.pkg.github.com/sriniketh7/AILogAnalyser</url>
    </repository>
</repositories>
```

---

## 3. Add the client dependency

```xml
<dependency>
    <groupId>com.proj1</groupId>
    <artifactId>ailoganlzr-client</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

Maven can then retrieve the client from GitHub Packages instead of requiring the AILogAnalyser project or JAR to exist locally.

---

## 4. Configure the AILogAnalyser server

Configure the client with the URL of the deployed AILogAnalyser server using the client configuration supported by the project.

The server is deployed independently from the host application.

Therefore:

```text
Maven dependency
      │
      ▼
ailoganlzr-client
      │
      │ HTTP
      ▼
Remote AILogAnalyser Server
```

The host application does not need to run PostgreSQL, PGVector, Redis or Gemini itself for AILogAnalyser's server-side processing.

---

## 5. Connect it to exception handling

The client is designed to be invoked from the host application's exception-handling flow.

Conceptually:

```java
@ExceptionHandler(Exception.class)
public ResponseEntity<?> handleGeneralException(Exception exception) {

    // Host application's existing exception handling

    // Invoke AILogAnalyser client for analysis

    ...

}
```

The host application's existing exception handling remains under the application's control.

---

# Running the Server Locally

The server depends on external infrastructure/configuration including:

- PostgreSQL,
- PGVector,
- Redis,
- and Gemini credentials.

After configuring the required application properties/environment variables, build the project from the repository root:

```bash
mvn clean package
```

The server module can then be started as a Spring Boot application.

---

# Docker

The server is containerized using Docker.

The deployment uses a multi-stage build so Maven builds the application inside the container build process before the final runtime image is produced.

This avoids requiring a pre-built local `target/*.jar` to exist before a remote Docker build.

---

# Deployment

The AILogAnalyser server is deployed on **Render**.

The deployed environment uses:

```text
AILogAnalyser Web Service
        │
        ├── PostgreSQL
        │      └── PGVector
        │
        ├── Redis
        │
        └── Gemini API
```

Runtime configuration such as database connectivity, Redis connectivity, server port and AI credentials is supplied through environment configuration rather than embedding deployment credentials in source code.

The client remains a separate Maven artifact distributed through GitHub Packages.

---

# Caching

Redis is used as the analysis cache.

This reduces repeated model calls when an analysis response is already available in the cache.

Cache behavior is instrumented as part of the application's metrics layer, including cache hits and misses.

---

# Observability

AILogAnalyser uses **Micrometer** for application-level metrics.

The project tracks analysis-related measurements such as:

- analysis requests,
- analysis execution time,
- cache hits,
- cache misses.

This makes the analysis pipeline observable beyond normal application logging.

---

# RAG Pipeline

The Retrieval-Augmented Generation pipeline can be summarized as:

```text
Knowledge Documents
       │
       ▼
TokenTextSplitter
       │
       ▼
Document Chunks
       │
       ▼
Embeddings
       │
       ▼
PGVector
       │
       │ similarity search
       ▼
Top Relevant Documents
       │
       ▼
Prompt Context
```

At runtime:

```text
Stack Trace
      │
      ▼
Similarity Search
      │
      ▼
Top 3 Relevant Documents
      │
      ├──────────────┐
      │              │
      ▼              ▼
Retrieved       Source / Execution
Knowledge          Context
      │              │
      └──────┬───────┘
             ▼
        Prompt Builder
             │
             ▼
           Gemini
```

---

# Source-Aware Analysis

A key part of AILogAnalyser is the distinction between **stack-trace analysis** and **source-aware stack-trace analysis**.

A traditional AI request could simply be:

```text
Stack trace → LLM
```

AILogAnalyser instead attempts to build:

```text
Stack trace
    +
relevant source code
    +
execution-flow context
    +
retrieved debugging knowledge
    ↓
LLM
```

The project uses stack-frame information and Java source parsing to connect runtime failures back to application code.

This provides the model with more context for reasoning about what happened in the application.

---

# Design Decisions

### Why separate the client and server?

Keeping the integration logic in a lightweight client and the analysis infrastructure on a server avoids requiring every consuming application to independently configure:

- an AI model,
- vector storage,
- Redis,
- analysis persistence,
- and the RAG pipeline.

The client focuses on the host application's context; the server focuses on analysis.

### Why PGVector?

The project already uses PostgreSQL for persistence. PGVector allows vector similarity search to live alongside the relational database infrastructure instead of introducing a separate vector database product.

### Why Redis?

AI analysis can be comparatively expensive and slow relative to a cache lookup. Redis allows previously cached analyses to be reused.

### Why JavaParser?

Stack traces identify classes, methods and line numbers, but they do not contain the corresponding method implementation. JavaParser allows the client to inspect Java source code structurally and extract useful code context.

### Why RAG?

A model's general knowledge does not necessarily contain the specific debugging guidance or knowledge that the application wants to provide.

RAG allows relevant knowledge to be retrieved dynamically and included in the model context.

---

# What This Project Demonstrates

AILogAnalyser was built to explore several backend and AI engineering concepts in one system:

- reusable Java library design,
- multi-module Maven projects,
- client-server architecture,
- Spring Boot auto-configuration/integration,
- global exception handling integration,
- stack-trace parsing,
- Java source-code analysis,
- external API communication,
- structured LLM responses,
- Retrieval-Augmented Generation,
- embeddings and vector similarity search,
- PostgreSQL/PGVector,
- Redis caching,
- persistence with JPA,
- application metrics,
- Docker containerization,
- cloud deployment,
- and Maven package distribution.

---

# Current Scope

AILogAnalyser currently focuses on **Java/Spring Boot application exception analysis**.

The project is an engineering/learning project and should not be interpreted as a replacement for full observability platforms, production APM tooling, or human debugging.

Its focus is narrower:

> Given an application exception, automatically collect useful debugging context and use that context to produce a more informed AI-assisted analysis.

---

# Repository Modules

```text
ailoganlzr-parent
       │
       ├── ailoganlzr-client
       │
       │      Host-application integration
       │
       │      Source-code extraction
       │
       │      Stack-trace processing
       │
       │      HTTP communication
       │
       │
       └── ailoganlzr-server
              Analysis orchestration
              PostgreSQL persistence
              Redis caching
              RAG / PGVector
              Spring AI / Gemini
              Metrics
```

---

# Status

The current implementation has been tested with a separate Spring Boot host application where:

```text
Host application
      ↓
GitHub Packages client
      ↓
Remote Render deployment
      ↓
Redis / PostgreSQL / PGVector
      ↓
Gemini analysis
```

The client artifact was also tested after removing the locally installed Maven artifact, confirming that the consuming application could resolve it from GitHub Packages.

---

# Author

**Challa Sriniketh**

Backend-focused software engineer working primarily with Java and Spring Boot.

GitHub: `sriniketh7`
