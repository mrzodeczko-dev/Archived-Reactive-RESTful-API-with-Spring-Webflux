# Reactive RESTful API — Spring WebFlux

> **Archived project** — built a few years ago as a learning exercise. It was **not** migrated
> to Spring Boot 3 or 4 and stays on its original **Spring Boot 2.4.4 / Java 17** baseline.
>
> Recent revisions kept on that baseline:
> - Restored compilation on Java 17 / Spring Boot 2.4.4
> - Closed several reactive-correctness gaps so the Netty event-loop is never blocked (BCrypt, JWT signing/parsing, CSV import, email — all offloaded to `Schedulers.boundedElastic()`)
> - Added 120 unit tests across all application services (Mockito + `StepVerifier`)
>
> ⚠️ **Migration note:** The Spring ecosystem changed significantly between Spring Boot 2.x and
> Spring Boot 3.x / 4.x. A real migration would require:
> - Full `javax.*` → `jakarta.*` package rename (breaking change in every file)
> - Spring Security 6 rewrite — `WebSecurityConfigurerAdapter` removed, replaced with `SecurityFilterChain` beans
> - Mongock API changes (4.x → 5.x driver model)
> - Spring Data MongoDB reactive API updates
> - Minimum Java version bumped to 17 (Boot 3) / 21 (Boot 4)
>
> The project is kept on its original baseline for reference and portfolio purposes.

---

## Table of Contents

1. [Overview](#overview)
2. [Business Domain](#business-domain)
3. [Role-Based Access Control](#role-based-access-control)
4. [Tech Stack](#tech-stack)
5. [Prerequisites](#prerequisites)
6. [Quick Start](#quick-start)
7. [OpenAPI / Swagger UI](#openapi--swagger-ui)
8. [Architecture](#architecture)
9. [MongoDB Replica Set](#mongodb-replica-set)
10. [Docker Commands](#docker-commands)
11. [Non-Blocking Integrations](#non-blocking-integrations)
12. [Tests](#tests)
13. [Why Reactive?](#why-reactive)
14. [Migration History](#migration-history)

---

## Overview

A reactive REST API built with **Domain-Driven Design (DDD)** on top of Spring WebFlux and Netty. The full I/O pipeline is non-blocking: WebFlux router + reactive MongoDB driver + replica set transactions — no blocking thread is ever held during a request.

This is a **cinema ticketing system** — a backend API for managing a network of cinemas and handling the full ticket purchasing flow. Users authenticate via JWT and are assigned one of two roles: **USER** or **ADMIN**.

---

[↑ Back to top](#table-of-contents)

## Business Domain

### User Flow

A typical user journey looks like this:

1. Browse cinemas available in their city
2. Pick a movie and find available screenings
3. Choose seats and place an order
4. Complete the purchase

### Domain Model

```mermaid
erDiagram
    CITY {
        string id
        string name
    }
    CINEMA {
        string id
        string name
    }
    CINEMA_HALL {
        string id
        string name
    }
    MOVIE {
        string id
        string title
        string genre
    }
    MOVIE_EMISSION {
        string id
        datetime startTime
        decimal price
    }
    TICKET {
        string id
        int row
        int seat
        string status
    }
    TICKET_ORDER {
        string id
        datetime orderedAt
        string status
    }
    TICKET_PURCHASE {
        string id
        datetime purchasedAt
        decimal totalPrice
    }
    USER {
        string id
        string email
        string role
    }

    CITY ||--o{ CINEMA : "1 city has many cinemas"
    CINEMA ||--o{ CINEMA_HALL : "1 cinema has many halls"
    CINEMA_HALL ||--o{ MOVIE_EMISSION : "1 hall hosts many screenings"
    MOVIE ||--o{ MOVIE_EMISSION : "1 movie has many screenings"
    MOVIE_EMISSION ||--o{ TICKET : "1 screening generates many tickets"
    TICKET_ORDER ||--o{ TICKET : "1 order contains many tickets"
    TICKET_ORDER ||--|| TICKET_PURCHASE : "1 order has 1 purchase"
    USER ||--o{ TICKET_ORDER : "1 user places many orders"
```

---

[↑ Back to top](#table-of-contents)

## Role-Based Access Control

| Endpoint | Public | USER | ADMIN |
|---|:---:|:---:|:---:|
| `POST /register` | ✅ | | |
| `POST /login` | ✅ | | |
| `GET /statistics/**` | ✅ | | |
| `/emails/**` | | ✅ | |
| `GET /cities/**` | | ✅ | |
| `GET /cinemas` | | ✅ | |
| `/movies/**` | | ✅ | ✅ |
| `/tickets/**` | | ✅ | |
| `/ticketOrders/**` | | ✅ | |
| `/ticketsOrders/**` | | ✅ | |
| `/ticketPurchases/**` | | ✅ | |
| `/movieEmissions/**` | | ✅ | ✅ |
| `/users/**` | | | ✅ |
| `/cinemas/**` | | | ✅ |
| `/admin/ticketPurchases/**` | | | ✅ |
| `POST /movies/csv` (bulk import) | | | ✅ |
| `POST /movieEmissions` | | | ✅ |

**Summary:**
- **Public** — registration, login, statistics, Swagger docs
- **USER** — browsing cinemas and movies, managing own tickets, orders, and purchases, sending emails
- **ADMIN** — managing users and cinemas, importing movies via CSV, creating screenings, viewing all purchases

---

[↑ Back to top](#table-of-contents)

## Tech Stack

### Core

| Layer | Technology | Version |
|---|---|---|
| Framework | Spring Boot | 3.5.13 |
| Reactive web | Spring WebFlux + Netty | via Boot |
| Reactive runtime | Project Reactor | via Boot |
| Java | Eclipse Temurin | 21 |

### Persistence

| Layer | Technology | Version |
|---|---|---|
| Database | MongoDB | 4.4.4 |
| Reactive driver | spring-boot-starter-data-mongodb-reactive | via Boot |
| Sync driver | mongodb-driver-sync | via Boot |
| DB migrations | Mongock | 5.4.4 |

### Security & Auth

| Layer | Technology | Version |
|---|---|---|
| Security | Spring Security (WebFlux) | via Boot |
| JWT | JJWT (api / impl / jackson) | 0.12.6 |

### Observability & Tooling

| Layer | Technology | Version |
|---|---|---|
| Logging | Log4j2 (spring-boot-starter-log4j2) | via Boot |
| API docs | springdoc-openapi WebFlux UI | 2.8.13 |
| Code generation | Lombok | 1.18.34 |

### Infrastructure

| Layer | Technology |
|---|---|
| Containerization | Docker |
| Local orchestration | Docker Compose |
| Production orchestration | Docker Swarm |
| Build tool | Maven 3.8+ |

---

[↑ Back to top](#table-of-contents)

## Prerequisites

- **Docker** and **Docker Compose** (Docker Swarm mode enabled for swarm deployment)
- **Java 21** + **Maven 3.8+** (for local build)

---

[↑ Back to top](#table-of-contents)

## Quick Start

```bash
# 1. Build the application (skip tests for speed)
mvn clean package -DskipTests

# 2. Start all containers (app + MongoDB replica set)
docker-compose up -d --build
```

---

[↑ Back to top](#table-of-contents)

## OpenAPI / Swagger UI

Interactive API documentation is available at:
http://localhost:8080/docs

text

---

[↑ Back to top](#table-of-contents)

## Architecture

The entire application is containerised. The layered Docker image build uses `maven-dependency-plugin` to split the fat JAR into **dependencies** and **classes** — dependencies are cached between builds, only changed classes are re-copied:

```mermaid
block-beta
  columns 1
  A["🔄  CLASSES — changes each build"]
  B["📦  DEPENDENCIES — cached layer"]
  C["☕  JRE  eclipse-temurin:21"]

  style A fill:#4f98a3,color:#fff,stroke:#1a626b
  style B fill:#2d6a6e,color:#fff,stroke:#1a626b
  style C fill:#1a3e40,color:#fff,stroke:#0f3638
```

Two Docker Compose files are provided:

| File | Purpose |
|---|---|
| `docker-compose.yml` | Local development — builds image from source |
| `docker-swarm.yml` | Production — deploys a stack to Docker Swarm |

---

[↑ Back to top](#table-of-contents)

## MongoDB Replica Set

The app uses **MongoDB distributed transactions**, which require a replica set. Three nodes are configured:

```mermaid
flowchart LR
    Client(["👤 Client"])

    subgraph ReplicaSet["MongoDB Replica Set"]
        direction TB
        P[("🟢 Primary\nreceives all writes")]
        S1[("🔵 Secondary 1\nreplicates oplog")]
        S2[("🔵 Secondary 2\nreplicates oplog")]
        P -- oplog --> S1
        P -- oplog --> S2
    end

    Client -- "write\n(always)" --> P
    Client -- "read\n(primaryPreferred)" --> P
    Client -. "read fallback" .-> S1
    Client -. "read fallback" .-> S2
```

All replica nodes are containerised with persistent Docker volumes.

---

[↑ Back to top](#table-of-contents)

## Docker Commands

### Docker Compose (development)

```bash
# Start all containers in the background
docker-compose up -d --build

# Follow logs
docker-compose logs -f

# Stop and remove containers, networks, volumes
docker-compose down
```

### Docker Swarm (production)

```bash
# Deploy stack
docker stack deploy -c docker-swarm.yml <appName>

# List stack tasks
docker stack ps <appName>

# Remove stack
docker stack rm <appName>
```

---

[↑ Back to top](#table-of-contents)

## Non-Blocking Integrations

Every code path that touches an inherently blocking or CPU-bound API is wrapped in `Mono.fromCallable(...)` and offloaded to Reactor's `Schedulers.boundedElastic()`. The Netty event-loop is never held by hashing, signing, parsing, file I/O, or SMTP work.

- **BCrypt password hashing** — both `PasswordEncoder.encode` (registration) and `PasswordEncoder.matches` (login) run on the bounded elastic scheduler. BCrypt is ~50–100 ms CPU-bound and would otherwise stall the event-loop on every login.
- **JWT issuance and verification** — HS512 signing in `AppTokensService.generateTokens` and claim parsing in `AppTokensService.getId` / `isTokenValid` (called by `AuthenticationManager` on every authenticated request) are executed on the bounded elastic scheduler.
- **Email sending** — `JavaMailSender.send` calls are offloaded to the bounded elastic scheduler. A retry policy (up to 2 attempts with exponential back-off) handles transient SMTP failures; authentication errors are excluded from retries.
- **CSV movie import** — OpenCSV parsing is offloaded via `Flux.using` (which guarantees `BufferedReader` closure even on cancellation) to the bounded elastic scheduler. Each row is validated and checked for uniqueness against MongoDB before writing. If any row fails, the entire import is rejected atomically — no partial saves occur.
- **Reactive MongoDB** — all persistence operations use the reactive driver natively, so no offload is required.

---

## Tests

Unit tests cover all application services and run in under 5 seconds without any external dependencies (MongoDB, SMTP, etc.) — collaborators are mocked with Mockito and reactive flows are asserted using `StepVerifier` from `reactor-test`.

```bash
mvn test
```

**Coverage:** 120 tests across 10 service test classes (`CinemaServiceTest`, `CinemaHallServiceTest`, `CityServiceTest`, `EmailServiceTest`, `MovieEmissionServiceTest`, `MovieServiceTest`, `StatisticsServiceTest`, `TicketOrderServiceTest`, `TicketPurchaseServiceTest`, `UsersServiceTest`).

---

[↑ Back to top](#table-of-contents)


## Why Reactive?

### WebFlux vs Project Loom — Virtual Threads

Java 21 introduced **Virtual Threads** (Project Loom, JEP 444) as a production-ready feature, which changed the calculus around reactive programming significantly.

| Use WebFlux when… | Use Virtual Threads (Spring MVC) when… |
|---|---|
| Full reactive stack: WebClient, R2DBC, reactive MongoDB | Stack uses JDBC / JPA / Hibernate / any blocking driver |
| Real-time streaming: SSE, WebSockets, Kafka consumer | Classic REST microservice |
| Backpressure control is required | Team prefers readable, debuggable synchronous code |
| API gateway / BFF / fan-out edge service | Using blocking third-party SDKs |
| Team is experienced with `Mono`/`Flux` | New project on Java 21+ |

> **Bottom line (2025–2026):** For most CRUD microservices touching a relational database, **Spring MVC + Virtual Threads** is now the pragmatic default. WebFlux remains the right choice for streaming workloads and fully non-blocking stacks.

- ✅ This project uses WebFlux **correctly** — the full stack is non-blocking (reactive MongoDB driver, no JDBC)
- ✅ Reactive Mongo with replica set transactions is a legitimate use case for WebFlux
- ⚠️ If this project were greenfield today and used a relational DB, **Spring MVC + Virtual Threads** would likely be the better choice

---

[↑ Back to top](#table-of-contents)

---

## Migration History

### Phase 1 — Java 17 → Java 21 (Spring Boot 2.x baseline)

The first phase kept the project on Spring Boot 2.x and only bumped the JDK from 17 to 21. The changes were minimal: the `maven-compiler-plugin` target was updated to Java 21, the base Docker image was switched to `eclipse-temurin:21`, and the `java.version` property in `pom.xml` was updated accordingly. No source code changes were required since the project did not use any APIs removed between Java 17 and 21.

### Phase 2 — Spring Boot 2.4.4 → Spring Boot 3.5.13 (Java 21)

The second phase was a full Spring Boot 3 migration. Spring Boot 3 requires Java 17 as a minimum and ships with the Jakarta EE 9 namespace, which meant a significant number of changes across the codebase:

- **`javax.*` → `jakarta.*`** — all Jakarta EE imports (e.g. `javax.mail.*`, `javax.crypto.*` where applicable) were renamed to `jakarta.*`. This was the most widespread change, touching mail configuration and security-related classes.
- **Spring Security 6** — `WebSecurityConfigurerAdapter` was removed in Spring Security 6. The security configuration was rewritten using `SecurityWebFilterChain` bean with the lambda DSL. A circular dependency between `WebSecurityConfig`, `AuthenticationManager`, and `AppTokensService` (caused by `PasswordEncoder` being defined in the security config) was resolved by moving `PasswordEncoder` to a dedicated `PasswordEncoderConfig` class.
- **Mongock 4.x → 5.x** — Mongock 4 (`mongock-spring-v5` + `mongodb-springdata-v3-driver`) was internally dependent on `javax.*` and incompatible with Spring Boot 3. The migration involved replacing the BOM and driver with `io.mongock:mongock-springboot-v3` and `io.mongock:mongodb-reactive-driver`, switching to the `@EnableMongock` auto-configuration, and replacing the `@ChangeLog` / `@ChangeSet` annotations with the new `@ChangeUnit` / `@Execution` / `@RollbackExecution` model.
- **JJWT 0.11.x → 0.12.6** — the entire JJWT builder and parser API was deprecated in 0.11 and removed in 0.12. All builder calls were updated to the new fluent API (`subject()`, `expiration()`, `issuedAt()` instead of `setSubject()` etc.), `parserBuilder()` was replaced with `parser()`, `parseClaimsJws()` with `parseSignedClaims()`, and `getBody()` with `getPayload()`. The `SecretKey` bean was updated from `Keys.secretKeyFor(SignatureAlgorithm.HS512)` to `Jwts.SIG.HS512.key().build()`.
- **MongoDB custom converter** — `PositionMapToBSONObjectConverter` was returning `org.bson.BSONObject` which is no longer recognised as a store-supported type in Spring Data MongoDB 4.x. The converter was updated to return `org.bson.Document` instead.
- **springdoc-openapi 1.x → 2.8.13** — the dependency was replaced with `springdoc-openapi-starter-webflux-ui` and `springdoc-openapi-starter-webflux-api`. The Swagger UI security permit-list in `WebSecurityConfig` was expanded to cover the new default paths (`/swagger-ui/**`, `/swagger-ui.html`, `/v3/api-docs`) used by springdoc 2.x, and `config-url` / `url` properties were added to `application.yml` to correctly wire the UI to the API spec.

### Phase 3 — Spring Boot 3.5.x / Java 21 → Spring Boot 4.0.6 / Java 25

- **Java 25** — bumped `java.version`, compiler target, and base Docker image to `eclipse-temurin:25`
- **Lombok 1.18.34 → 1.18.38** — 1.18.34 crashes on Java 24+ (`TypeTag :: UNKNOWN`); 1.18.38 is the first release with JDK 24/25 support
- **Jackson 2 → 3** — SB4 ships Jackson 3 (`tools.jackson` group); replaced all `com.fasterxml.jackson.*` imports and `JsonProcessingException` → `JacksonException` in `GlobalExceptionHandler`, `GlobalWebExceptionHandler`, `WebSecurityConfig`; updated `jackson-dataformat-yaml` artifact group accordingly
- **`ErrorWebExceptionHandler` removed** — `org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler` no longer exists; replaced with `org.springframework.web.server.WebExceptionHandler`; `setRawStatusCode(int)` → `setStatusCode(HttpStatus)`
- **MongoDB config prefix** — `spring.data.mongodb.*` deprecated; migrated to `spring.mongodb.uri` with database name inlined in the URI
- **AOP starter** — `spring-aspects` replaced with `spring-boot-starter-aspectj` (new dedicated starter in SB4)
- **`pom.xml` cleanup** — removed obsolete `com.github.cloudyrock.mongock` BOM, dropped `junit-vintage-engine` exclusion (no longer shipped by SB4)