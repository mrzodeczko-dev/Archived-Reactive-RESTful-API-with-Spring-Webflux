# Migration Guide: Spring Boot 2.4 → Spring Boot 4 + Java 25

## Overview

This document describes all changes made to migrate the project from
**Spring Boot 2.4.4 / Java 17** to **Spring Boot 4.0.5 / Java 25**.

---

## 1. `pom.xml`

| What | Before | After |
|---|---|---|
| Spring Boot | `2.4.4` | `4.0.5` |
| Java | `17` | `25` |
| Lombok | `1.18.34` | `1.18.36` |
| jjwt | `0.11.2` | `0.13.0` |
| springdoc | `1.5.2` (webflux-ui / webflux-core) | `2.8.9` (starter-webflux-ui only) |
| mongock groupId | `com.github.cloudyrock.mongock` | `io.mongock` |
| mongock version | `4.2.8.BETA` | `5.4.4` |
| spring-aspects | version pinned to `5.3.1` | managed by Spring Boot BOM |
| maven-compiler-plugin | `3.13.0` | `3.14.0` |
| BlockHound | `1.0.6.RELEASE` | **removed** (incompatible with Java 25) |
| `spring-boot-maven-plugin` `<fork>true</fork>` | present | **removed** (deprecated in Boot 4) |

---

## 2. `Dockerfile`

- Base image: `eclipse-temurin:17-jre` → `eclipse-temurin:25-jre`
- Removed all `--add-opens` flags (no longer needed; BlockHound removed)

---

## 3. `CinemaApplication.java` — SecretKey bean

jjwt 0.12 removed `Keys.secretKeyFor(SignatureAlgorithm)`. Use the new type-safe SIG API:

```java
// Before
Keys.secretKeyFor(SignatureAlgorithm.HS512)

// After
Jwts.SIG.HS512.key().build()
```

---

## 4. `AppTokensService.java` — jjwt 0.13 builder & parser API

```java
// Builder — method renames
.setSubject(id)     →  .subject(id)
.setExpiration(date) →  .expiration(date)
.setIssuedAt(date)   →  .issuedAt(date)

// Parser — full rewrite
Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody()
→
Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload()
```

---

## 5. `WebSecurityConfig.java` — Spring Security 7 lambda DSL

Spring Security 7 removed the deprecated chained DSL. All configuration must
use the lambda form:

```java
// Before (Spring Security 5/6 deprecated style)
http.csrf().disable()
    .authorizeExchange()
    .pathMatchers(...).permitAll()
    .and()
    .build();

// After (Spring Security 7 — lambda DSL only)
http
    .csrf(csrf -> csrf.disable())
    .authorizeExchange(exchanges -> exchanges
        .pathMatchers(...).permitAll()
    )
    .build();
```

---

## 6. Mongock 4 → Mongock 5

### `InitScripts.java`

```java
// Before
import io.changock.migration.api.annotations.ChangeLog;
import io.changock.migration.api.annotations.ChangeSet;

@ChangeLog(order = "1")
public class InitScripts {
    @ChangeSet(order = "001", id = "createAdmin", author = "CoderNoOne")
    public void createAdmin(...) { ... }
}

// After
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;

@ChangeUnit(id = "createAdmin", order = "1", author = "CoderNoOne")
public class InitScripts {
    @Execution
    public void createAdmin(...) { ... }

    @RollbackExecution
    public void rollbackCreateAdmin(...) { ... }
}
```

### `MongoMigrationConfiguration.java`

```java
// Before
import com.github.cloudyrock.mongock.driver.mongodb.springdata.v3.SpringDataMongoV3Driver;
import com.github.cloudyrock.spring.v5.MongockSpring5;

MongockSpring5.builder()
    .setDriver(SpringDataMongoV3Driver.withDefaultLock(mongoTemplate))
    .addChangeLogsScanPackage(pkg)
    ...
    .buildInitializingBeanRunner();

// After
import io.mongock.driver.mongodb.springdata.v4.SpringDataMongoV4Driver;
import io.mongock.runner.springboot.MongockSpringboot;

MongockSpringboot.builder()
    .setDriver(SpringDataMongoV4Driver.withDefaultLock(mongoTemplate))
    .addMigrationScanPackage(pkg)
    ...
    .buildInitializingBeanRunner();
```

---

## 7. SpringDoc — artifact rename

SpringDoc 2.x (required for Spring Boot 3+/4+) consolidated the webflux artifacts:

```xml
<!-- Before (springdoc 1.x) -->
<artifactId>springdoc-openapi-webflux-ui</artifactId>
<artifactId>springdoc-openapi-webflux-core</artifactId>

<!-- After (springdoc 2.x) -->
<artifactId>springdoc-openapi-starter-webflux-ui</artifactId>
<!-- core is now included transitively -->
```

---

## 8. Known remaining steps before production

- [ ] Verify Jackson 3 serialization for all DTOs (Spring Boot 4 ships Jackson 3 by default)
- [ ] Run full integration test suite against a live MongoDB instance
- [ ] Update `docker-compose.yml` MongoDB image to `mongo:8` if applicable
- [ ] Review any `@Document` / `@TypeAlias` changes introduced in Spring Data MongoDB 5
