# Implementation Plan: Event Module (Node.js to Java Spring Boot)

## 1. System Architecture Overview
The Event module will follow a standard Java Spring Boot Layered Architecture:
- **Controllers (`@RestController`)**: Handle HTTP requests, validation (`@Valid`), and route to services.
- **Services (`@Service`)**: Contain core business logic, transaction management (`@Transactional`), and cross-module communication.
- **Repositories (`@Repository`)**: Spring Data JPA interfaces for database operations.
- **Entities (`@Entity`)**: JPA models mapped to the relational database tables.
- **DTOs & Mappers**: Data Transfer Objects for request/response payloads to avoid exposing internal entities. MapStruct or manual mappers will be used.

## 2. Database Schema Mapping (JPA Entities)

### 2.1. Event Entity (`Event.java`)
- **Fields**:
    - `Long eventId` (`@Id`, `@GeneratedValue`)
    - `String eventName`, `String genre`, `String description`, `String eventImgUrl`
    - `LocalDateTime dateToStart`, `LocalDateTime timeToStart`, `LocalDateTime timeToRelease`
    - `Integer duration`
    - `String status` (Enum: PENDING, APPROVED, REJECTED, IN_PROGRESS, CANCELLED, ENDED)
    - `JsonNode seatLayoutMap` (Use `@JdbcTypeCode(SqlTypes.JSON)` or `@Convert`)
- **Relationships**:
    - `@ManyToOne` -> `Organizer` (Assume exists from auth/user module)
    - `@ManyToOne` -> `Venue`
    - `@OneToMany(mappedBy = "event")` -> `TicketClassItem`
    - `@OneToMany(mappedBy = "event")` -> `Seat`

### 2.2. TicketClass Entity (`TicketClass.java`)
- **Fields**:
    - `Long ticketClassId` (`@Id`, `@GeneratedValue`)
    - `String className`, `Double price`, `Integer quota`, `String description`, `String color`, `String type` (SEATED/STANDING)
- **Relationships**:
    - `@ManyToOne` -> `Event`

### 2.3. Seat Entity (`Seat.java`)
- **Fields**:
    - `Long seatId` (`@Id`, `@GeneratedValue`)
    - `String name` (e.g., "VIP-A-R1C1")
    - `String status` (AVAILABLE, BOOKED, LOCKED)
- **Relationships**:
    - `@ManyToOne` -> `Event`
    - `@ManyToOne` -> `TicketClassItem`

### 2.4. Related Entities (Assumed existing or minimal setup)
- `Venue`: `venueId`, `venueName`, `address`
- `Order` & `Ticket`: Needed for Dashboard and Customer queries.

## 3. Core API Endpoints (Controllers)

### 3.1. Public Event Controller (`PublicEventController.java`)
- `GET /api/events` -> `findAll()`: List APPROVED/IN_PROGRESS events.
- `GET /api/events/venues` -> `findVenues()`: List all venues.
- `GET /api/events/{id}` -> `findOne(id)`: Get event details with ticket classes & seats.

### 3.2. Customer Event Controller (`CustomerEventController.java`)
- `GET /api/events/my-tickets` -> `findMyEventsCustomer(userId)`: List events and purchased tickets for the logged-in customer. (Requires `@PreAuthorize("hasRole('CUSTOMER')")`)

### 3.3. Organizer Event Controller (`OrganizerEventController.java`)
*(All endpoints require `@PreAuthorize("hasRole('ORGANIZER')")`)*
- `GET /api/events/my-event` -> `findOrganizerDashboard(userId)`: Get dashboard stats, alerts, and events list.
- `GET /api/events/my-event/{id}` -> `findOneOrganizerEvent(eventId, userId)`: Get specific event details.
- `POST /api/events/my-event` -> `createBasicInfoEvent(EventRequestDTO)`: Create Step 1 (PENDING status, validates timeline).
- `POST /api/events/my-event/{id}/ticket-classes` -> `createTicketClasses(eventId, List<TicketClassDTO>)`: Create Step 2.
- `PUT /api/events/my-event/{id}/ticket-classes` -> `editTicketClasses(eventId, List<TicketClassDTO>)`: Handle both create (new tickets) and update (existing tickets) via transaction.
- `GET /api/events/my-event/{id}/ticket-classes` -> `getTicketClassesByEvent(eventId)`: Get ticket classes for Step 3.
- `POST /api/events/my-event/{id}/layout` -> `createLayout(eventId, LayoutRequestDTO)`: Create Step 3 (Save JSON, generate physical `Seat` records).
- `PUT /api/events/my-event/{id}` -> `updateEvent(eventId, EventUpdateDTO)`: Update basic info, status (CANCELLED), and ticket class prices/quotas.

## 4. Implementation Phases

- [ ] **Phase 1: Domain Modeling & Repositories**
    - [ ] Map Prisma schemas to `@Entity` classes (Event, TicketClass, Seat, Venue).
    - [ ] Create Spring Data JPA Repositories (`EventRepository`, `TicketClassRepository`, `SeatRepository`, `VenueRepository`).
    - [ ] Write complex JPQL/Native queries for the Organizer Dashboard stats (aggregating Order, Ticket, Seat data).

- [ ] **Phase 2: DTOs & Exception Handling**
    - [ ] Create Request/Response DTOs mapping to Node.js `req.body` and outputs.
    - [ ] Implement global exception handling (`@RestControllerAdvice`) to handle `EntityNotFoundException` (mapping to `NotFoundError`) and `IllegalArgumentException` (mapping to `BadRequestError`).

- [ ] **Phase 3: Basic Event Services**
    - [ ] Implement `EventService.findAll()` and `EventService.findOne()`.
    - [ ] Implement `EventService.createBasicInfoEvent()` with timeline validations (`timeToRelease < timeToStart`, etc.).
    - [ ] Implement `TicketClassService.createTicketClasses()` and `editTicketClasses()` ensuring `@Transactional`.

- [ ] **Phase 4: Advanced Services (Layout & Dashboard)**
    - [ ] Implement `EventService.createLayout()`: Parse JSON map, calculate seat coordinates, drop deleted seats, and batch insert `Seat` entities (`saveAll`).
    - [ ] Implement `EventService.update()`: Add logic to prevent `venueId` updates if layout exists.
    - [ ] Implement `EventService.getOrganizerDashboard()`: Replicate Node.js aggregation logic (total revenue, tickets sold, fill rate, alerts).

- [ ] **Phase 5: Controllers & Security Integrations**
    - [ ] Build REST Controllers.
    - [ ] Implement custom security checks (e.g., `isOwnerEvent` logic implemented as a `@Component` or directly in Service layer validating `event.getOrganizer().getUserId() == currentUserId`).

## 5. Technical Challenges & Optimizations
- **Seat Generation Batching**: The layout generation in `createLayout` can create thousands of seats. Use `JpaRepository.saveAll()` and configure `spring.jpa.properties.hibernate.jdbc.batch_size` in `application.properties` to ensure efficient batch inserts (similar to Prisma's `createMany`).
- **Dashboard Aggregations**: The Node.js code does heavy data fetching and in-memory reshaping for `getOrganizerDashboard` and `findMyEventsCustomer`. In Spring Boot, utilize JPQL `@Query` with `GROUP BY` or `@EntityGraph` to avoid the N+1 query problem and offload grouping to the database where possible.
- **JSON Field Mapping**: Use a library like `Hypersistence Utils` (formerly Vlad Mihalcea's Hibernate Types) to easily map the `seatLayoutMap` JSON structure to a `JsonNode` or custom POJO object.
- **Transaction Management**: Methods like `editTicketClasses` and `createLayout` modify multiple tables. Must be annotated with `@Transactional(rollbackFor = Exception.class)` to guarantee atomic operations.