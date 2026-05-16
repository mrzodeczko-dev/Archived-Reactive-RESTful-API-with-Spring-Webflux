# Reactive RESTful API – Cinema Ticketing Platform (Spring WebFlux)

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-25-orange.svg)](https://openjdk.org/)
[![WebFlux](https://img.shields.io/badge/Spring-WebFlux-6db33f.svg)](https://docs.spring.io/spring-framework/reference/web/webflux.html)
[![MongoDB](https://img.shields.io/badge/MongoDB-Replica%20Set-green.svg)](https://www.mongodb.com/)
[![Docker](https://img.shields.io/badge/Docker-Ready-blue.svg)](https://www.docker.com/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

> **Archived project.** Originally built as a learning exercise on Spring Boot 2.4.4 / Java 17, then iteratively migrated to **Spring Boot 4.0.5 / Java 25** and refactored into a hexagonal / DDD-inspired layout. Kept for reference and portfolio purposes — see [Migration History](#migration-history).

<a id="toc"></a>
## Table of Contents

- [Overview](#overview)
- [How It Works](#how-it-works)
- [Business Domain](#business-domain)
- [Role-Based Access Control](#role-based-access-control)
- [API Endpoints](#api-endpoints)
- [Getting Started](#getting-started)
- [Environment Variables](#environment-variables)
- [Architecture](#architecture)
- [MongoDB Replica Set](#mongodb-replica-set)
- [Non-Blocking Integrations](#non-blocking-integrations)
- [Technical Highlights](#technical-highlights)
- [Tech Stack](#tech-stack)
- [Testing](#testing)
- [CI Pipeline](#ci-pipeline)
- [Observability](#observability)
- [Repository Structure](#repository-structure)
- [Why Reactive?](#why-reactive)
- [Contact](#contact)

---

<a id="overview"></a>
## Overview

[↑ Back to top](#toc)

A reactive REST API for a **cinema ticketing system** — manages a network of cinemas and the full ticket purchasing flow (browse cities → cinemas → screenings → seats → order → purchase). The full I/O pipeline is non-blocking: **Spring WebFlux** on Netty, **reactive MongoDB driver** with a 3-node replica set for distributed transactions, and JWT-based authentication. Every CPU-bound or blocking call (BCrypt, JWT signing, CSV parsing, SMTP) is explicitly offloaded to `Schedulers.boundedElastic()`.

The codebase follows a **hexagonal / DDD-inspired** layering: a `domain` layer with plain Java entities free of Spring/Mongo/Lombok annotations; an `application` layer that orchestrates use cases against `port/out` interfaces; and an `infrastructure` layer with reactive Mongo adapters, security, AOP, and migrations. HTTP routing lives in a `presentation` layer using functional `RouterFunction` + handler beans.

> **DDD status:** the domain package is genuinely free of Spring imports, Mongo annotations, and Lombok; persistence concerns are isolated in `infrastructure/persistence` (separate `*Document` classes + mappers + repository adapters). Application services operate on Reactor `Mono`/`Flux` directly — this is by design rather than a limitation, since `Mono`/`Flux` signatures are necessary to compose a fully non-blocking pipeline end-to-end. This is best described as **DDD-inspired hexagonal layering with Reactor**, rather than a textbook framework-agnostic clean architecture.

---

<a id="how-it-works"></a>
## How It Works

[↑ Back to top](#toc)

End-to-end flow for the canonical use case — a registered user purchasing a ticket:

```mermaid
sequenceDiagram
    participant C as Client
    participant API as WebFlux API
    participant Sec as Security Filter<br/>(JWT)
    participant H as Handler
    participant S as Service
    participant DB as MongoDB Replica Set
    participant M as JavaMailSender

    C->>+API: POST /register {username, email, password}
    API->>+H: UsersHandler.register
    H->>+S: UsersService.register
    S->>S: BCrypt encode (boundedElastic)
    S->>+DB: insert user
    DB-->>-S: User
    S-->>-H: UserDto
    H-->>-API: 201 Created
    API-->>-C: UserDto

    C->>+API: POST /login {username, password}
    API->>+H: LoginHandler.login
    H->>+S: AuthenticationManager
    S->>S: BCrypt matches (boundedElastic)
    S->>S: JWT sign HS512 (boundedElastic)
    S-->>-H: access + refresh token
    H-->>-API: 200 OK
    API-->>-C: { accessToken, refreshToken }

    C->>+API: POST /ticketOrders<br/>Authorization: Bearer ...
    API->>+Sec: SecurityContextRepository
    Sec->>Sec: parse + verify JWT (boundedElastic)
    Sec-->>-API: Authenticated principal
    API->>+H: TicketOrderHandler.orderTickets
    H->>+S: TicketOrderService (reactive Mongo, replica set tx)
    S->>+DB: reserve seats + persist order (transaction)
    DB-->>-S: TicketOrder
    S-->>-H: TicketOrderDto
    H-->>-API: 201 Created
    API-->>-C: TicketOrderDto

    C->>+API: POST /ticketPurchases/ticketOrderId/{id}
    API->>+H: TicketPurchaseHandler
    H->>+S: TicketPurchaseService
    S->>+DB: mark order purchased (transaction)
    DB-->>-S: TicketPurchase
    S->>+M: send confirmation email (boundedElastic + retry)
    M-->>-S: ack
    S-->>-H: TicketPurchaseDto
    H-->>-API: 201 Created
    API-->>-C: TicketPurchaseDto
```

1. **Registration** — public endpoint; BCrypt hashing runs on `boundedElastic`.
2. **Login** — issues JWT access token (HS512, 5 min) + refresh token (8 h); signing runs on `boundedElastic`.
3. **Token refresh** — `POST /refresh` accepts the refresh JWT in the request body, validates its signature and expiry, verifies it is not an access token (via the `AccessTokenKey` claim), then issues a new access + refresh token pair (token rotation). Runs on `boundedElastic`.
4. **Authenticated requests** — `SecurityContextRepository` + `AuthenticationManager` parse the bearer token and populate the reactive security context.
5. **Ticket ordering** — reserves seats atomically inside a **MongoDB distributed transaction**.
6. **Ticket purchase** — finalises an order in a transaction, then sends a confirmation email via SMTP (offloaded to `boundedElastic` with retries).

---

<a id="business-domain"></a>
## Business Domain

[↑ Back to top](#toc)

A typical user journey: **browse cinemas in their city → pick a movie → find a screening → choose seats → place an order → complete the purchase.**

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

Domain entities (`com.rzodeczko.domain`) are plain immutable Java records — no Spring/Mongo/Lombok. Application services in `com.rzodeczko.application.service` orchestrate use cases against output ports. Persistence representations live in `infrastructure.persistence.document` (`*Document` classes with `@Document` + Lombok), mapped to/from domain by dedicated mappers.

---

<a id="role-based-access-control"></a>
## Role-Based Access Control

[↑ Back to top](#toc)

Authentication is JWT-based. Each account has role **USER** or **ADMIN**. ADMIN can be granted via `POST /users/promoteToAdmin/username/{username}`.

| Endpoint | Public | USER | ADMIN |
|---|:---:|:---:|:---:|
| `POST /register` | ✅ | | |
| `POST /login` | ✅ | | |
| `POST /refresh` | ✅ | | |
| `/docs`, `/v3/api-docs/**` (Swagger) | ✅ | | |
| `/actuator/health` | ✅ | | |
| `GET /cities/**` | | ✅ | |
| `GET /cinemas` | | ✅ | |
| `/movies/**` | | ✅ | ✅ |
| `/tickets/**` | | ✅ | |
| `/ticketOrders/**` | | ✅ | |
| `/ticketsOrders/**` | | ✅ | |
| `/ticketPurchases/**` | | ✅ | |
| `/movieEmissions/**` (read) | | ✅ | ✅ |
| `POST /emails/send/single` | | ✅ | ✅ |
| `/users/**` | | | ✅ |
| `/statistics/**` | | | ✅ |
| `/cinemas/**` (write) | | | ✅ |
| `POST /movieEmissions` | | | ✅ |
| `/admin/ticketPurchases/**` | | | ✅ |
| `POST /emails/send/multiple` | | | ✅ |
| `POST /cities/csv` (bulk import) | | | ✅ |
| `POST /cinemas/csv` (bulk import) | | | ✅ |
| `POST /cinemaHalls/cinemaId/{id}/csv` | | | ✅ |
| `POST /movies/csv` (bulk import) | | | ✅ |
| `POST /movieEmissions/csv` (bulk import) | | | ✅ |

---

<a id="api-endpoints"></a>
## API Endpoints

[↑ Back to top](#toc)

Base URL (local): `http://localhost:8080`. Authentication via `Authorization: Bearer <accessToken>`.

### Auth & Users

| Method | Path | Description | Roles |
|---|---|---|---|
| `POST` | `/register` | Create a new account | Public |
| `POST` | `/login` | Issue access + refresh JWTs | Public |
| `POST` | `/refresh` | Rotate tokens using a valid refresh JWT | Public |
| `GET` | `/users` | List all users | ADMIN |
| `GET` | `/users/username/{username}` | Get user by username | ADMIN |
| `POST` | `/users/promoteToAdmin/username/{username}` | Grant ADMIN role | ADMIN |

### Cities, Cinemas, Halls

| Method | Path | Description | Roles |
|---|---|---|---|
| `POST` | `/cities` | Create city | ADMIN |
| `GET` | `/cities` | List cities | USER |
| `GET` | `/cities/name/{name}` | Find city by name | USER |
| `PUT` | `/cities` | Attach a cinema to a city | ADMIN |
| `POST` | `/cities/csv` | Bulk import from CSV | ADMIN |
| `POST` | `/cinemas` | Create cinema | ADMIN |
| `GET` | `/cinemas` | List cinemas | USER |
| `GET` | `/cinemas/city/{city}` | List cinemas in a city | USER |
| `PUT` | `/cinemas/id/{id}/addCinemaHall` | Add hall to cinema | ADMIN |
| `POST` | `/cinemas/csv` | Bulk import from CSV | ADMIN |
| `GET` | `/cinemaHalls` | List all halls | USER |
| `GET` | `/cinemaHalls/cinemaId/{cinemaId}` | List halls of a cinema | USER |
| `POST` | `/cinemaHalls/addToCinema/cinemaId/{cinemaId}` | Add hall | ADMIN |
| `POST` | `/cinemaHalls/cinemaId/{cinemaId}/csv` | Bulk import halls from CSV | ADMIN |

### Movies & Screenings

| Method | Path | Description | Roles |
|---|---|---|---|
| `GET` | `/movies` | List all movies | USER / ADMIN |
| `GET` | `/movies/id/{id}` | Get movie by id | USER / ADMIN |
| `POST` | `/movies` | Add a movie | ADMIN |
| `DELETE` | `/movies/id/{id}` | Delete a movie | ADMIN |
| `PATCH` | `/movies/addToFavorites/{id}` | Add to user's favorites | USER |
| `GET` | `/movies/favorites` | List user's favorites | USER |
| `GET` | `/movies/filter/premiereDate` | Filter by premiere date | USER / ADMIN |
| `GET` | `/movies/filter/duration` | Filter by duration | USER / ADMIN |
| `GET` | `/movies/filter/name/{name}` | Filter by name | USER / ADMIN |
| `GET` | `/movies/filter/genre/{genre}` | Filter by genre | USER / ADMIN |
| `GET` | `/movies/filter/keyword/{keyword}` | Keyword filter (name + genre) | USER / ADMIN |
| `POST` | `/movies/csv` | Bulk import from CSV (atomic) | ADMIN |
| `POST` | `/movieEmissions` | Schedule a screening | ADMIN |
| `GET` | `/movieEmissions` | List all screenings | USER / ADMIN |
| `GET` | `/movieEmissions/movieId/{movieId}` | Screenings of a movie | USER / ADMIN |
| `GET` | `/movieEmissions/cinemaHallId/{cinemaHallId}` | Screenings in a hall | USER / ADMIN |
| `DELETE` | `/movieEmissions/{id}` | Cancel a screening | ADMIN |
| `POST` | `/movieEmissions/csv` | Bulk import from CSV | ADMIN |

### Orders & Purchases

| Method | Path | Description | Roles |
|---|---|---|---|
| `POST` | `/ticketOrders` | Place a ticket order | USER |
| `PUT` | `/ticketsOrders/cancel/orderId/{orderId}` | Cancel an order | USER |
| `GET` | `/ticketsOrders/username` | List logged user's orders | USER |
| `POST` | `/ticketPurchases` | Buy a ticket directly | USER |
| `POST` | `/ticketPurchases/ticketOrderId/{id}` | Finalise an existing order | USER |
| `GET` | `/ticketPurchases` | Logged user's purchases | USER |
| `GET` | `/ticketPurchases/city/{city}` | …filtered by city | USER |
| `GET` | `/ticketPurchases/cinemaId/{cinemaId}` | …filtered by cinema | USER |
| `GET` | `/ticketPurchases/movieId/{movieId}` | …filtered by movie | USER |
| `GET` | `/admin/ticketPurchases` | All purchases | ADMIN |
| `GET` | `/admin/ticketPurchases/dates` | All purchases by date range | ADMIN |
| `GET` | `/admin/ticketPurchases/city/{city}` | All purchases by city | ADMIN |
| `GET` | `/admin/ticketPurchases/cinemaId/{cinemaId}` | …by cinema | ADMIN |
| `GET` | `/admin/ticketPurchases/cinemaHallId/{cinemaHallId}` | …by hall | ADMIN |
| `GET` | `/admin/ticketPurchases/movieId/{movieId}` | …by movie | ADMIN |

### Email & Statistics

| Method | Path | Description | Roles |
|---|---|---|---|
| `POST` | `/emails/send/single` | Send email to self | USER / ADMIN |
| `POST` | `/emails/send/multiple` | Send batch to multiple recipients | ADMIN |
| `GET` | `/statistics/cities/cinemaFrequency` | Cinema count per city | ADMIN |
| `GET` | `/statistics/cities/cinemaFrequency/max` | City with most cinemas | ADMIN |
| `GET` | `/statistics/movies/mostPopular/byCity` | Most popular movie per city | ADMIN |
| `GET` | `/statistics/movies/frequency` | Per-movie ticket frequency | ADMIN |
| `GET` | `/statistics/movies/mostPopularGroupedByGenre/byCity/{city}` | Top movies per genre in a city | ADMIN |
| `GET` | `/statistics/averageTicketPrice` | Average ticket price per city | ADMIN |

> Browse the interactive contract at **Swagger UI** (`http://localhost:8080/docs`) once the application is running.

---

<a id="getting-started"></a>
## Getting Started

[↑ Back to top](#toc)

### Prerequisites

- **Docker** and **Docker Compose v2**
- **Java 25** + **Maven 3.9+** _(only if running outside containers)_

### 1. Provide environment variables

```bash
cp .env.sample .env
# fill in real values
```

The `.env` file must sit next to `docker-compose.yml` (loaded automatically) and must not be committed (covered by `.gitignore`). See [Environment Variables](#environment-variables) for the full list.

### 2. Build the application

```bash
mvn clean package -DskipTests
```

### 3. Start the stack

```bash
docker compose up -d --build
```

Brings up: `mongo1` / `mongo2` / `mongo3` (replica set), `mongo-init` (one-shot bootstrapper), `liquibase-mongo` (migrations), `app` (WebFlux service). Each starts only after its dependency is healthy.

### 4. Verify

| Resource | URL |
|----------|-----|
| API | `http://localhost:8080` |
| Swagger UI | `http://localhost:8080/docs` |
| OpenAPI JSON | `http://localhost:8080/v3/api-docs` |
| Actuator health | `http://localhost:8080/actuator/health` |

```bash
curl -i http://localhost:8080/actuator/health          # → 200 {"status":"UP"}
docker exec -it mongo1 mongosh --port 30001 --eval "rs.status().ok"   # → 1
```

---

<a id="environment-variables"></a>
## Environment Variables

[↑ Back to top](#toc)

Copy `.env.sample` to `.env` and fill in real values. The file is git-ignored.

| Variable | Description |
|----------|-------------|
| `MAIL_USERNAME` | SMTP username (`spring.mail.username`) |
| `MAIL_PASSWORD` | SMTP password (Gmail app password by default) |
| `ADMIN_USERNAME` / `ADMIN_PASSWORD` | Bootstrap admin account |
| `MONGO1_HOST` / `MONGO2_HOST` / `MONGO3_HOST` | Hostnames for replica set nodes |
| `MONGO_PORT` | In-container port for all Mongo nodes |
| `MONGO1_HOST_PORT` / `MONGO2_HOST_PORT` / `MONGO3_HOST_PORT` | Host ports published per node |
| `MONGO_DB_NAME` | MongoDB database name |
| `RS_NAME` | Replica set name (e.g. `rs0`) |
| `JWT_SECRET_KEY` | HS512 signing key |
| `APP_PORT` | Spring Boot listening port |

---

<a id="architecture"></a>
## Architecture

[↑ Back to top](#toc)

Hexagonal / DDD-inspired layering with a strict dependency direction (`presentation → application → domain`; `infrastructure` provides adapters for application ports):

```mermaid
graph TD
    Client(["Client / HTTP Request"]) --> NETTY["Netty event-loop<br/>(Spring WebFlux)"]
    NETTY --> REQID["RequestIdWebFilter<br/>(X-Request-Id tracing)"]
    REQID --> SEC["SecurityContextRepository<br/>+ AuthenticationManager (JWT)"]
    SEC --> ROUTER["*Routing beans<br/>(RouterFunction per resource domain)"]
    ROUTER --> H["Handlers<br/>(UsersHandler, MoviesHandler, …)"]
    H --> APP["Application Services<br/>(use-case orchestration)"]
    APP --> PORTS["application.port.out<br/>(CinemaPort, MailPort, TransactionPort, …)"]
    APP --> DOM["Domain Layer<br/>(plain Java entities, VOs)"]
    PORTS -.implemented by.-> ADAPTERS["Reactive Mongo Adapters<br/>(infrastructure.persistence.repository.impl)"]
    ADAPTERS --> MAPPER["Document ↔ Domain<br/>Mappers"]
    MAPPER --> MDB[("MongoDB Replica Set<br/>via reactive driver")]
    APP --> MAILPORT["MailPort"]
    MAILPORT -.implemented by.-> MAILADAPTER["JavaMailSenderAdapter<br/>(infrastructure.mail)"]
    MAILADAPTER -.boundedElastic.-> SMTP{{"SMTP server"}}
    APP --> CSVPORT["*CsvParserPort"]
    CSVPORT -.implemented by.-> CSVADAPTER["Csv*ParserAdapter<br/>(infrastructure.csv)"]
    MONGOCK["Liquibase / Mongock<br/>@ChangeUnit migrations"] --> MDB
```

### Layer responsibilities

| Layer | Package | Responsibility |
|---|---|---|
| Presentation | `com.rzodeczko.presentation` | HTTP routing (`*Routing` classes extending `BaseJsonRouter`), handler beans with `@Operation`/`@ApiResponses`, springdoc wiring via `@RouterOperations`. |
| Application | `com.rzodeczko.application` | Use-case orchestration, DTO ↔ domain mapping, input validation, output port interfaces. `Mono`/`Flux` are used in port and service signatures. |
| Domain | `com.rzodeczko.domain.*` | Immutable Java records, value objects (`Money`, `Discount`, `Position`). No Spring / Mongo / Lombok imports. |
| Infrastructure | `com.rzodeczko.infrastructure.*` | Port implementations: reactive Mongo repositories, `*Document` types, security configuration, CSV parser adapters, Mongock migrations, AOP logging, HTTP filter. |

> The Docker image is **layered**: `maven-dependency-plugin unpack` splits the fat JAR into a cached dependencies layer and a small per-build classes layer.

---

<a id="mongodb-replica-set"></a>
## MongoDB Replica Set

[↑ Back to top](#toc)

MongoDB distributed transactions require a replica set. Three nodes run in Docker with persistent volumes (`./data/mongo-{1,2,3}`):

```mermaid
flowchart LR
    Client(["Reactive driver"])

    subgraph ReplicaSet["MongoDB Replica Set rs0"]
        direction TB
        P[("🟢 Primary mongo1<br/>receives all writes")]
        S1[("🔵 Secondary mongo2<br/>replicates oplog")]
        S2[("🔵 Secondary mongo3<br/>replicates oplog")]
        P -- oplog --> S1
        P -- oplog --> S2
    end

    Client -- "writes" --> P
    Client -- "reads (primaryPreferred)" --> P
    Client -. "read fallback" .-> S1
    Client -. "read fallback" .-> S2
```

The `mongo-init` container waits for all three nodes to respond, then runs `rs.initiate(...)` on `mongo1` (idempotent). `liquibase-mongo` runs only after `mongo-init` completes; `app` starts only after `liquibase-mongo` completes.

Connection string (from `application.yml`):
```
mongodb://${MONGO1_HOST}:${MONGO_PORT},${MONGO2_HOST}:${MONGO_PORT},${MONGO3_HOST}:${MONGO_PORT}/${MONGO_DB_NAME}?replicaSet=${RS_NAME}
```

MongoDB image: **`mongo:8.3.1`**.

### Migrations: Liquibase + Docker Container

Database migrations are applied via a **Liquibase container** (`liquibase-mongo` service in `docker-compose.yml`). This container:
- Runs the official Liquibase CLI with the MongoDB extension (`liquibase-mongodb`).
- Applies YAML-based changesets from `db/changelog/` directory.
- Executes before the `app` service starts (dependency chain in Compose).
- Changesets are versioned and tracked in MongoDB's `DATABASECHANGELOG` collection.

To add a migration:
1. Create a new YAML file in `db/changelog/` (e.g., `db/changelog/001-initial-schema.yaml`).
2. Define changesets with `@id` and `@author` attributes.
3. Re-build and restart the stack: `docker compose up -d --build`.

Example changeset structure:
```yaml
databaseChangeLog:
  - changeSet:
      id: "001-create-users"
      author: "system"
      changes:
        - insert:
            collectionName: "users"
            # … insert/update operations
```

---

<a id="non-blocking-integrations"></a>
## Non-Blocking Integrations

[↑ Back to top](#toc)

Every CPU-bound or blocking call is wrapped in `Mono.fromCallable(...)` and offloaded to `Schedulers.boundedElastic()`:

| Operation | Location | Notes |
|---|---|---|
| BCrypt hashing / matching | `UsersService`, `AuthenticationManager` | Offloaded to avoid blocking Netty thread |
| JWT issuance & verification (HS512) | `AppTokensService` | Signing/parsing via `Mono.fromCallable(...).subscribeOn(...)` |
| Email sending (blocking SMTP) | `JavaMailSenderAdapter` | With exponential backoff retry; consider adding circuit-breaker |
| CSV parsing (OpenCSV, synchronous) | `Csv*ParserAdapter` — errors collected before any DB write | All parsing on boundedElastic |
| MongoDB persistence | _No offload needed_ — reactive driver is non-blocking natively | Operations stay on Netty |

---

<a id="technical-highlights"></a>
## Technical Highlights

[↑ Back to top](#toc)

- **Fully reactive stack** — Spring WebFlux on Netty + reactive MongoDB driver; no JDBC, no blocking thread held during a request.
- **Functional routing** — per-resource `*Routing` classes extend `BaseJsonRouter`; springdoc wired via `@RouterOperations` on each router `@Bean`.
- **MongoDB distributed transactions** — three-node replica set; seat reservation and purchase are atomic across collections via `TransactionPort` (`TransactionalOperator`-backed).
- **Schedulers discipline** — every CPU-bound or blocking call explicitly offloaded to `Schedulers.boundedElastic()`.
- **Liquibase migrations** — versioned YAML changesets applied by a dedicated Compose service before the app starts; tracked in MongoDB.
- **Async admin bootstrap with health gate** — `AdminBootstrapper` runs asynchronously (non-blocking startup) with retry/backoff; `AdminBootstrapHealthIndicator` keeps `/actuator/health` in DOWN state until bootstrap succeeds. This prevents traffic to an app without an admin account.
- **JWT with token rotation** — HS512-signed access tokens (5 min) + refresh tokens (8 h). `POST /refresh` validates the refresh JWT, rejects access tokens passed by mistake (via the `AccessTokenKey` claim check), and issues a fresh token pair on every call (rotation).
- **Hexagonal layering** — domain free of Spring / Mongo / Lombok; ports in `application.port.out`, adapters in `infrastructure`; services expose `Mono`/`Flux` for end-to-end pipeline composition.
- **Immutable domain objects** — Java records with "wither" methods; value objects validate invariants in the canonical constructor.
- **Request ID tracing** — `RequestIdWebFilter` attaches a UUID `X-Request-Id` to every request; echoed in response headers and included in every error body.
- **AOP logging** — `@Loggable` on handler methods triggers `@Around` advice that logs args (sensitive DTOs redacted), reactive signal type, and execution time.
- **Atomic CSV import** — bulk import either fully succeeds or rejects with a collected list of row-level errors; no partial saves.
- **Reactive error handling** — security error handlers use reactive chains with proper error handling (no fire-and-forget `.subscribe()`); all async operations have fallback/error recovery.

---

<a id="tech-stack"></a>
## Tech Stack

[↑ Back to top](#toc)

| Concern | Technology | Version |
|---|---|---|
| Language | Java (Eclipse Temurin) | 25 |
| Framework | Spring Boot | 4.0.6 |
| Reactive web | Spring WebFlux + Netty | via Boot |
| Reactive runtime | Project Reactor | via Boot |
| Database | MongoDB (replica set) | 8.3.1 |
| Reactive driver | `spring-boot-starter-data-mongodb-reactive` | via Boot |
| DB migrations | Mongock (`mongodb-springdata-v4-driver`) | 5.4.4 |
| Security | Spring Security (WebFlux) | via Boot |
| JWT | JJWT (`jjwt-api` / `-impl` / `-jackson`) | 0.12.x |
| Logging | Log4j2 (Logback excluded) | via Boot |
| API docs | `springdoc-openapi-starter-webflux-ui` / `-api` | 2.8.13 |
| CSV | OpenCSV | — |
| AOP | `spring-boot-starter-aspectj` | via Boot |
| Code generation | Lombok (persistence + DTOs only, not in domain) | — |
| Containerisation | Docker (layered, Eclipse Temurin 25 JRE) + Compose v2 | — |
| Build | Maven 3.9+ | — |

---

<a id="testing"></a>
## Testing

[↑ Back to top](#toc)

Three independent test suites run as separate Maven profiles and CI jobs:

### Unit tests — application services

Plain POJO services, no Spring context; collaborators mocked with **Mockito**, reactive flows asserted with **StepVerifier**. Runs in under 5 seconds.

```bash
mvn test
```

Located in `src/test/java/com/rzodeczko/application/service/` (e.g., `CinemaServiceTest`, `MovieServiceTest`, `TicketOrderServiceTest`, …).

### Handler slice tests (`it-handlers`)

`@WebFluxTest` spins up routing + handler only; services mocked via `@MockitoBean`. **Security is replaced with a no-op filter chain** (see `AbstractHandlerSliceTest.Configs#noOpFilterChain`), so slice tests focus on HTTP routing, handler logic, and response shape — not authorization.

```bash
mvn verify -P it-handlers -DskipUTs=true
```

Located in `src/test/java/it/handlers/` (e.g., `LoginHandlerSliceTest`, `CitiesHandlerSliceTest`, …).

### Repository integration tests (`it-testcontainers`)

Spins up a real MongoDB replica set via **Testcontainers** and verifies repository adapters end-to-end — custom Mongo converters, aggregation pipelines, reactive query methods.

```bash
mvn verify -P it-testcontainers -DskipUTs=true
```

Located in `src/test/java/it/testcontainers/repository/` (e.g., `UserRepositoryImplIT`, `MovieRepositoryImplIT`, …).

---

<a id="ci-pipeline"></a>
## CI Pipeline

[↑ Back to top](#toc)

`.github/workflows/ci.yml` (if present) compiles the project, runs all three suites **in parallel**, then merges JaCoCo execution data for a unified Codecov report:

```mermaid
flowchart LR
    A[Build] --> B1[Unit tests]
    A --> B2[Handler slice tests]
    A --> B3[Repository integration tests]

    B1 --> C[JaCoCo merge & report]
    B2 --> C
    B3 --> C
```

Each suite uploads its `.exec` file as a GitHub Actions artifact. The `coverage` job merges them, enforces thresholds with `jacoco:check`, and uploads to Codecov. `fail-fast: true` cancels remaining jobs on any failure.

---

<a id="observability"></a>
## Observability

[↑ Back to top](#toc)

### Request ID tracing

`RequestIdWebFilter` runs at `Ordered.HIGHEST_PRECEDENCE`. It reads or generates an `X-Request-Id` UUID per request, stores it as an exchange attribute, echoes it in the response header, and includes it in every error body (`GlobalExceptionHandler` + security error handlers) for client-side log correlation.

### AOP logging

`@Loggable` on handler methods triggers an `@Around` aspect (`LoggerAspect`) that logs method name, arguments (sensitive DTOs like `CreateUserDto` are `[REDACTED]`), reactive signal type (`CANCEL`, `ON_COMPLETE`, `ON_ERROR`), and execution time — without blocking the pipeline.

### Structured error responses

All 4xx/5xx errors return:

```json
{
  "error": { "message": "…" },
  "requestId": "3f2a1b…"
}
```

5xx messages are always generic `"Internal server error. Please try again later."` — the real exception is logged server-side only.

### Security error handling

`WebSecurityConfig` defines custom `ServerAuthenticationEntryPoint` and `ServerAccessDeniedHandler`:
- **401 Unauthorized** — logs the reason server-side with request ID; returns generic message to client.
- **403 Forbidden** — logs principal name + path + reason (async, with fallback for retrieval failures); returns generic "Access denied" to client.

Both handlers use reactive chains with proper error recovery — no fire-and-forget `.subscribe()` calls.

### Health indicators

- **`adminBootstrap` health indicator** — reports DOWN until admin bootstrap completes successfully. This prevents Docker Compose healthcheck from marking the container as healthy until the bootstrap is ready.
- **Standard Spring Boot health** — Actuator exposes only `health` endpoint (`management.endpoints.web.exposure.include: health`).
- **Healthcheck** — Docker Compose polls `/actuator/health` every 15 seconds (5 s timeout, 5 retries, 60 s start period). App is not marked healthy until `adminBootstrap` indicator is UP.

---

<a id="repository-structure"></a>
## Repository Structure

[↑ Back to top](#toc)

```
.
├── src/
│   ├── main/
│   │   ├── java/com/rzodeczko/
│   │   │   ├── CinemaApplication.java
│   │   │   │
│   │   │   ├── domain/                               # Pure business — no Spring/Mongo/Lombok
│   │   │   │   ├── cinema/                           # Cinema record + Builder
│   │   │   │   ├── cinema_hall/                      # CinemaHall record
│   │   │   │   ├── city/                             # City record
│   │   │   │   ├── exception/                        # DiscountException (domain-level)
│   │   │   │   ├── generic/                          # GenericEntity marker interface
│   │   │   │   ├── movie/                            # Movie record + enums/MovieGenre
│   │   │   │   ├── movie_emission/                   # MovieEmission record
│   │   │   │   ├── ticket/                           # Ticket record + enums/
│   │   │   │   ├── ticket_order/                     # TicketOrder record + enums/
│   │   │   │   ├── ticket_purchase/                  # TicketPurchase record
│   │   │   │   ├── user/                             # User record
│   │   │   │   └── vo/                               # Money, Discount, Position value objects
│   │   │   │
│   │   │   ├── application/
│   │   │   │   ├── dto/                              # Request / response DTOs
│   │   │   │   │   └── contract/                     # TicketDtoMarker sealed interface
│   │   │   │   ├── exception/                        # Application-layer exceptions
│   │   │   │   ├── mapper/                           # DTO ↔ domain mappers (static methods)
│   │   │   │   ├── port/
│   │   │   │   │   └── out/                          # Output ports: CinemaPort, MailPort,
│   │   │   │   │                                     # TransactionPort, PasswordEncoderPort,
│   │   │   │   │                                     # *CsvParserPort (5), PersistencePort<T,ID>
│   │   │   │   ├── security/
│   │   │   │   │   └── enums/                        # Role enum
│   │   │   │   ├── service/                          # Use-case orchestration (10 service classes)
│   │   │   │   │   ├── enums/                        # UserField
│   │   │   │   │   └── util/                         # ServiceUtils, DateTimeGapFinder
│   │   │   │   └── validator/                        # Per-DTO validators (plain Java)
│   │   │   │       ├── generic/                      # Validator<T> interface
│   │   │   │       └── util/                         # Validations, TicketBaseValidationUtils
│   │   │   │
│   │   │   ├── infrastructure/
│   │   │   │   ├── aspect/                           # LoggerAspect (@Loggable AOP)
│   │   │   │   │   └── annotations/                  # @Loggable
│   │   │   │   ├── config/                           # ApplicationBeansConfig, AppConfigurationProperties
│   │   │   │   ├── csv/                              # 5 Csv*ParserAdapter + Csv*Row classes
│   │   │   │   ├── mail/                             # JavaMailSenderAdapter (MailPort impl)
│   │   │   │   ├── openapi/                          # OpenApiConfig
│   │   │   │   ├── persistence/
│   │   │   │   │   ├── config/                       # ReactiveMongoConfig, ConvertersConfig
│   │   │   │   │   │   └── converter/                # 9 custom Mongo converters
│   │   │   │   │   ├── document/                     # *Document types — @Document + Lombok
│   │   │   │   │   ├── initscripts/                  # Mongock @ChangeUnit migrations + AdminBootstrapper
│   │   │   │   │   ├── mapper/                       # Document ↔ Domain mappers
│   │   │   │   │   └── repository/
│   │   │   │   │       ├── Mongo*Repository.java     # Spring Data reactive interfaces
│   │   │   │   │       └── impl/                     # *RepositoryImpl — application port adapters
│   │   │   │   ├── security/
│   │   │   │   │   ├── AuthenticationManager.java
│   │   │   │   │   ├── SecurityContextRepository.java
│   │   │   │   │   ├── SpringPasswordEncoderAdapter.java
│   │   │   │   │   ├── config/                       # WebSecurityConfig, PasswordEncoderConfiguration,
│   │   │   │   │   │                                 # SecretKeyConfig
│   │   │   │   │   └── tokens/                       # AppTokensService (JJWT, boundedElastic)
│   │   │   │   ├── transaction/                      # ReactiveTransactionAdapter (TransactionPort impl)
│   │   │   │   └── web/                              # RequestIdWebFilter
│   │   │   │
│   │   │   └── presentation/
│   │   │       ├── csv/                              # CsvMultipartFileReader
│   │   │       ├── exception/                        # GlobalExceptionHandler
│   │   │       └── routing/
│   │   │           ├── BaseJsonRouter.java
│   │   │           ├── CinemaHallsRouting.java
│   │   │           ├── CinemasRouting.java
│   │   │           ├── CitiesRouting.java
│   │   │           ├── EmailRouting.java
│   │   │           ├── LoginRouting.java
│   │   │           ├── MovieEmissionsRouting.java
│   │   │           ├── MoviesRouting.java
│   │   │           ├── StatisticsRouting.java
│   │   │           ├── TicketOrdersRouting.java
│   │   │           ├── TicketPurchasesRouting.java
│   │   │           ├── UsersRouting.java
│   │   │           ├── handlers/                     # *Handler beans
│   │   │           └── userprovider/                 # CurrentUserProvider
│   │   │
│   │   └── resources/
│   │       └── application.yml
│   │
│   └── test/
│       ├── java/
│       │   ├── com/rzodeczko/application/service/    # Unit tests (Mockito + StepVerifier)
│       │   └── it/
│       │       ├── handlers/                         # Handler slice tests (@WebFluxTest)
│       │       └── testcontainers/
│       │           └── repository/                   # Repository IT (Testcontainers + real MongoDB)
│       └── resources/
│           ├── application-handlers.yml
│           ├── application-testcontainers.yml
│           └── log4j2-test.xml
│
├── csv-samples/                                      # Sample CSV files for all 5 bulk-import endpoints
├── db/                                               # Liquibase changelog + Dockerfile-liquibase
├── docker-compose.yml
├── Dockerfile
├── pom.xml
└── .env.sample
```

---

<a id="why-reactive"></a>
## Why Reactive?

[↑ Back to top](#toc)

### WebFlux vs Project Loom — Virtual Threads

Java 21+ introduced **Virtual Threads** (Project Loom, JEP 444), which changed the calculus around reactive programming significantly.

| Use WebFlux when… | Use Virtual Threads (Spring MVC) when… |
|---|---|
| Full reactive stack: WebClient, R2DBC, reactive MongoDB | Stack uses JDBC / JPA / any blocking driver |
| Real-time streaming: SSE, WebSockets, Kafka consumer | Classic REST microservice |
| Backpressure control is required | Team prefers readable, debuggable synchronous code |
| API gateway / fan-out edge service | New project on Java 21+ with blocking SDKs |

- ✅ This project uses WebFlux **correctly** — the full stack is non-blocking (reactive MongoDB driver, no JDBC).
- ✅ Reactive Mongo with replica-set transactions is a legitimate WebFlux use case.
- ⚠️ For a greenfield project on a relational DB, **Spring MVC + Virtual Threads** would likely be the better choice today.

---

<a id="contact"></a>
## Contact

[↑ Back to top](#toc)

Designed and implemented by **Michał Rzodeczko**.  
Other projects: [github.com/mrzodeczko-dev](https://github.com/mrzodeczko-dev)