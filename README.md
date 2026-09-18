# Ticket Booking API

A REST API backend for event ticket booking. It handles multi-role user auth, event and seat management, and concurrent seat reservations without double-booking. Built with Spring Boot 4 and Java 21, backed by PostgreSQL on [Neon](https://neon.tech).

---

## Techniques

**Compare-and-Swap (CAS) seat reservation**
The core booking logic uses a conditional `UPDATE ... WHERE status = 'AVAILABLE'` query instead of application-level locking. PostgreSQL executes the update atomically — only one concurrent transaction wins per row. The returned row count tells the service whether all requested seats were acquired, triggering a full rollback if not. See [`OrderRepository`](src/main/java/com/nghiatr/ticket_booking/order/repository/OrderRepository.java) and [`OrderService`](src/main/java/com/nghiatr/ticket_booking/order/service/OrderService.java).

**Scheduled seat release with a CronJob safety net**
Seats held in `PENDING` state expire after 5 minutes. A scheduled job runs every 30 seconds and releases them back to `AVAILABLE`. The release query includes `AND s.status = 'PENDING'` to prevent accidentally freeing a seat that was legitimately booked between the find and update steps of the job. See [`ReleaseExpiredSeatJob`](src/main/java/com/nghiatr/ticket_booking/shared/jobs/expired_seats/ReleaseExpiredSeatJob.java).

**Facade pattern for multi-service operations**
Operations that span more than one service — like creating a seat layout (which touches both `EventService` and `SeatService`) — are coordinated in [`EventFacade`](src/main/java/com/nghiatr/ticket_booking/orchestration/EventFacade.java). Controllers that need cross-service orchestration call the facade instead of wiring multiple services directly.

**State machine validation for event lifecycle**
[`EventActionValidator`](src/main/java/com/nghiatr/ticket_booking/event/validation/EventActionValidator.java) enforces a rule set against the current event status before any mutation is allowed — venue changes are blocked once a layout exists, ticket quota can only increase after approval, and editing is locked once an event is live or ended.

**Centralized exception handling with `@RestControllerAdvice`**
[`GlobalExceptionHandler`](src/main/java/com/nghiatr/ticket_booking/shared/handler/GlobalExceptionHandler.java) catches `AppException`, Bean Validation failures, and `DataIntegrityViolationException` in one place, mapping them to consistent JSON error responses without try/catch in controllers.

**JWT with separate access and refresh token flows**
[`JWTService`](src/main/java/com/nghiatr/ticket_booking/auth/security/JWTService.java) issues short-lived HS256 access tokens and longer-lived refresh tokens stored in the database. On login, all existing refresh tokens for that user are revoked before issuing a new one. The refresh endpoint rotates only the access token, keeping the existing refresh token alive until its own expiry.

**Micrometer metrics with Prometheus histograms**
`OrderService` tracks seat reservation latency as a Micrometer [Timer](https://micrometer.io/docs/concepts#_timers) with p50/p95/p99 percentiles, and counts both successful orders and rejected-due-to-unavailability events as separate [Counter](https://micrometer.io/docs/concepts#_counters) metrics. These feed into Prometheus and Grafana via the Actuator `/prometheus` endpoint.

**HikariCP connection pool tuning**
[`application.yaml`](src/main/resources/application.yaml) configures HikariCP with explicit pool sizing (`max=20`, `min-idle=10`) and timeout values rather than relying on defaults. This matters directly under concurrent booking load.

---

## Libraries & Technologies

| Library / Tool | Purpose |
|---|---|
| [Spring Boot 4](https://spring.io/projects/spring-boot) | Application framework, dependency injection, MVC, scheduling |
| [Spring Data JPA](https://spring.io/projects/spring-data-jpa) | Repository abstraction over Hibernate / PostgreSQL |
| [Spring Security](https://spring.io/projects/spring-security) | Stateless JWT filter chain, role-based access control |
| [JJWT 0.12.6](https://github.com/jwtk/jjwt) | JWT parsing and signing (HS256). Uses the modern fluent builder API introduced in 0.12 |
| [Lombok](https://projectlombok.org) | Compile-time code generation: `@Builder`, `@RequiredArgsConstructor`, `@Getter`/`@Setter` |
| [spring-dotenv](https://github.com/paulschwarz/spring-dotenv) | Loads `.env` files into Spring's `Environment` — keeps secrets out of `application.yaml` |
| [Micrometer + Prometheus registry](https://micrometer.io) | Application metrics, exposed via Spring Actuator |
| [Grafana](https://grafana.com) | Dashboard for visualizing Prometheus metrics |
| [PostgreSQL (Neon)](https://neon.tech) | Primary database; `pgcrypto` extension used for UUID generation |
| [HikariCP](https://github.com/brettwooldridge/HikariCP) | JDBC connection pooling (bundled with Spring Boot) |

---

## Project Structure

```
ticket-booking/
├── docker/
│   ├── prometheus/
│   └── graphana/
├── plans/
├── src/
│   └── main/
│       ├── java/com/nghiatr/ticket_booking/
│       │   ├── auth/
│       │   ├── event/
│       │   ├── orchestration/
│       │   ├── order/
│       │   ├── payment/
│       │   ├── seat/
│       │   ├── shared/
│       │   ├── ticket/
│       │   ├── ticketClass/
│       │   ├── user/
│       │   └── venue/
│       └── resources/
├── .env
├── docker-compose.monitoring.yml
└── pom.xml
```

**`docker/`** — Prometheus config (`prometheus.yml`) and a placeholder Grafana directory. Spin up the monitoring stack with `docker compose -f docker-compose.monitoring.yml up -d` after the app is running.

**`orchestration/`** — Contains [`EventFacade`](src/main/java/com/nghiatr/ticket_booking/orchestration/EventFacade.java), the only coordination point for operations that span multiple domain services in a single transaction.

**`shared/`** — Cross-cutting infrastructure: global exception handler, `ApiResponse`/`ErrorCode` DTOs, the scheduled expired-seat release job, and security utilities.

**`seat/`** — Entity, repository, and service for the seat grid. Seat layout is defined as a JSON column on the `events` table and materialized as individual `seat` rows when a layout is published.

**`resources/`** — Contains [`schema.sql`](src/main/resources/schema.sql) with the full PostgreSQL DDL including enum types, indexes, and constraints. Useful as a reference for the data model independent of Hibernate's `ddl-auto`.
