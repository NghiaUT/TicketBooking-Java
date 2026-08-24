-- ============================================================
-- Booking System Schema — PostgreSQL DDL (Neon compatible)
-- Generated from DBML diagram
-- ============================================================

-- Neon supports pgcrypto for gen_random_uuid()
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ============================================================
-- ENUM TYPES
-- ============================================================

CREATE TYPE ticket_type AS ENUM ('SEATED', 'STANDING');

CREATE TYPE ticket_status AS ENUM ('ACTIVE', 'USED', 'CANCELLED');

CREATE TYPE seat_status AS ENUM ('AVAILABLE', 'BOOKED', 'PENDING');

CREATE TYPE event_status AS ENUM (
  'PENDING',
  'APPROVED',
  'REJECTED',
  'IN_PROGRESS',
  'CANCELLED',
  'ENDED'
);

CREATE TYPE order_status AS ENUM ('PENDING', 'PAID', 'CANCELLED');

CREATE TYPE user_role AS ENUM ('ADMIN', 'ORGANIZER', 'CUSTOMER');

CREATE TYPE payment_status AS ENUM ('SUCCESS', 'FAILED', 'PENDING');

-- ============================================================
-- TABLE: users
-- ============================================================

CREATE TABLE users (
                       id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       name                 VARCHAR,
                       phone_number         VARCHAR UNIQUE,
                       email                VARCHAR UNIQUE,
                       password             VARCHAR,
                       role                 user_role DEFAULT 'CUSTOMER',
                       is_profile_complete  BOOLEAN DEFAULT FALSE,
                       is_deleted           BOOLEAN DEFAULT FALSE,
                       google_id            VARCHAR,
                       created_at           TIMESTAMP DEFAULT now()
);

-- ============================================================
-- TABLE: admins (1-1 extension of users)
-- ============================================================

CREATE TABLE admins (
                        user_id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE
);

-- ============================================================
-- TABLE: organizers (1-1 extension of users)
-- ============================================================

CREATE TABLE organizers (
                            user_id               UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
                            tax_code              VARCHAR,
                            website_url           VARCHAR,
                            business_license_url  VARCHAR,
                            is_approved           BOOLEAN DEFAULT FALSE,
                            reject_reason         VARCHAR
);

-- ============================================================
-- TABLE: customers (1-1 extension of users)
-- ============================================================

CREATE TABLE customers (
                           user_id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE
);

-- ============================================================
-- TABLE: venues
-- ============================================================

CREATE TABLE venues (
                        venue_id      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                        venue_name    VARCHAR NOT NULL,
                        address       VARCHAR,
                        capacity      INTEGER,
                        map_image_url VARCHAR
);

-- ============================================================
-- TABLE: events
-- ============================================================

CREATE TABLE events (
                        event_id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                        event_name       VARCHAR,
                        organizer_id     UUID NOT NULL REFERENCES organizers(user_id),
                        venue_id         UUID NOT NULL REFERENCES venues(venue_id),
                        genre            VARCHAR,
                        event_img_url    VARCHAR,
                        approved_by      UUID REFERENCES admins(user_id),
                        description      VARCHAR,
                        status           event_status DEFAULT 'PENDING',
                        time_to_start    TIMESTAMP,
                        date_to_start    TIMESTAMP,
                        time_to_release  TIMESTAMP,
                        duration         VARCHAR,
                        seat_layout_map  JSON,
                        reject_reason    VARCHAR
);

CREATE INDEX idx_events_organizer_id ON events(organizer_id);
CREATE INDEX idx_events_venue_id ON events(venue_id);
CREATE INDEX idx_events_status ON events(status);

-- ============================================================
-- TABLE: ticket_classes
-- ============================================================

CREATE TABLE ticket_classes (
                                ticket_class_id  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                event_id         UUID NOT NULL REFERENCES events(event_id) ON DELETE CASCADE,
                                class_name       VARCHAR,
                                price            DOUBLE PRECISION NOT NULL,
                                quota            INTEGER,
                                type             ticket_type DEFAULT 'SEATED',
                                color            VARCHAR,
                                description      VARCHAR,
                                UNIQUE (event_id, class_name)
);

CREATE INDEX idx_ticket_classes_event_id ON ticket_classes(event_id);

-- ============================================================
-- TABLE: orders
-- (created before seats/tickets since they reference it)
-- ============================================================

CREATE TABLE orders (
                        order_id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                        customer_id     UUID NOT NULL REFERENCES customers(user_id),
                        total_amount    DOUBLE PRECISION NOT NULL,
                        num_ticket      INTEGER,
                        status          order_status DEFAULT 'PENDING',
                        expired_at      TIMESTAMP,
                        customer_email  VARCHAR,
                        customer_name   VARCHAR,
                        customer_phone  VARCHAR
);

CREATE INDEX idx_orders_customer_id_status ON orders(customer_id, status);

-- ============================================================
-- TABLE: seats
-- ============================================================

CREATE TABLE seats (
                       seat_id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       event_id         UUID NOT NULL REFERENCES events(event_id) ON DELETE CASCADE,
                       ticket_class_id  UUID NOT NULL REFERENCES ticket_classes(ticket_class_id),
                       order_id         UUID REFERENCES orders(order_id),
                       name             VARCHAR,
                       status           seat_status DEFAULT 'AVAILABLE',
                       hold_expired_at  TIMESTAMP,
                       UNIQUE (event_id, name)
);

CREATE INDEX idx_seats_event_id_status ON seats(event_id, status);
CREATE INDEX idx_seats_ticket_class_id ON seats(ticket_class_id);
CREATE INDEX idx_seats_order_id ON seats(order_id);

-- ============================================================
-- TABLE: payments
-- ============================================================

CREATE TABLE payments (
                          payment_id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          order_id          UUID NOT NULL REFERENCES orders(order_id) ON DELETE CASCADE,
                          amount            DOUBLE PRECISION,
                          method            VARCHAR,
                          transaction_code  VARCHAR,
                          status            payment_status DEFAULT 'PENDING'
);

CREATE INDEX idx_payments_order_id ON payments(order_id);

-- ============================================================
-- TABLE: tickets
-- ============================================================

CREATE TABLE tickets (
                         ticket_id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                         order_id          UUID NOT NULL REFERENCES orders(order_id),
                         seat_id           UUID REFERENCES seats(seat_id),
                         ticket_class_id   UUID NOT NULL REFERENCES ticket_classes(ticket_class_id),
                         qr_dynamic_token  VARCHAR UNIQUE,
                         status            ticket_status DEFAULT 'ACTIVE'
);

CREATE INDEX idx_tickets_order_id ON tickets(order_id);
CREATE INDEX idx_tickets_seat_id ON tickets(seat_id);
CREATE INDEX idx_tickets_ticket_class_id ON tickets(ticket_class_id);

-- ============================================================
-- TABLE: refresh_tokens
-- ============================================================

CREATE TABLE refresh_tokens (
                                id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                token        VARCHAR NOT NULL,
                                user_id      UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                expiry_date  TIMESTAMP,
                                revoked      BOOLEAN DEFAULT FALSE,
                                created_at   TIMESTAMP DEFAULT now()
);

CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);

-- ============================================================
-- END OF SCRIPT
-- ============================================================