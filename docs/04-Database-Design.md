# Database Design Document

# Movie Ticket Management System

**Document:** Database Design  
**Version:** 1.0  
**Status:** Design Baseline  
**Implementation Status:** Database implementation postponed  
**Application:** Java Swing Desktop Application  
**Language:** Java 21  
**Database:** MySQL  
**Connectivity:** JDBC  
**Build Tool:** Maven  
**IDE:** IntelliJ IDEA

---

# 1. Purpose

This document defines the planned database structure for the Movie Ticket Management System.

It is intended to serve as the database reference for:

- Java model classes
- DAO classes
- JDBC implementation
- ER diagram
- GUI development
- Future MySQL implementation
- Antigravity development

The database does **not need to be implemented at this stage**. The GUI and application structure may be developed using mock/sample data first.

When MySQL implementation begins, the database must follow this document unless an approved requirement changes.

---

# 2. Database Name

The planned database name is:

```text
movie_ticket_management
```

---

# 3. Database Design Goals

The database should:

1. Store user authentication information.
2. Store movie information.
3. Store theatre information.
4. Store show information.
5. Store theatre seat information.
6. Store ticket booking information.
7. Store seats associated with each booking.
8. Maintain primary-key and foreign-key relationships.
9. Avoid unnecessary duplication of data.
10. Support CRUD operations.
11. Support future JDBC integration.
12. Support the booking workflow defined in the SRS.

---

# 4. Entity List

The planned database contains seven main entities:

```text
1. users
2. movies
3. theatres
4. shows
5. seats
6. bookings
7. booking_seats
```

---

# 5. Entity Relationship Overview

```text
                         ┌──────────────┐
                         │    USERS     │
                         └───────┬──────┘
                                 │
                               1:N
                                 │
                                 ▼
                         ┌──────────────┐
                         │   BOOKINGS   │
                         └───────┬──────┘
                                 │
                               1:N
                                 │
                                 ▼
                      ┌────────────────────┐
                      │   BOOKING_SEATS    │
                      └─────────┬──────────┘
                                │
                              N:1
                                │
                                ▼
                         ┌──────────────┐
                         │    SEATS     │
                         └───────┬──────┘
                                 │
                               N:1
                                 │
                                 ▼
                         ┌──────────────┐
                         │   THEATRES   │
                         └───────┬──────┘
                                 │
                               1:N
                                 │
                                 ▼
                         ┌──────────────┐
                         │    SHOWS     │
                         └───────┬──────┘
                                 │
                               N:1
                                 │
                                 ▼
                         ┌──────────────┐
                         │    MOVIES    │
                         └──────────────┘
```

---

# 6. Table 1 — Users

## Purpose

Stores users who can access the application.

## Structure

| Column | Data Type | Constraints | Description |
|---|---|---|---|
| user_id | INT | PK, AUTO_INCREMENT | Unique user ID |
| username | VARCHAR(50) | NOT NULL, UNIQUE | Login username |
| password | VARCHAR(255) | NOT NULL | Login credential |
| role | VARCHAR(20) | NOT NULL | User role |

## Roles

```text
ADMIN
USER
```

## Planned Example

| user_id | username | password | role |
|---:|---|---|---|
| 1 | admin | admin123 | ADMIN |
| 2 | user1 | user123 | USER |

> For a real production system, passwords should be securely hashed rather than stored as plain text. For a college prototype, the exact authentication implementation can be finalized during development.

---

# 7. Table 2 — Movies

## Purpose

Stores information about movies available in the system.

## Structure

| Column | Data Type | Constraints | Description |
|---|---|---|---|
| movie_id | INT | PK, AUTO_INCREMENT | Unique movie ID |
| title | VARCHAR(100) | NOT NULL | Movie title |
| genre | VARCHAR(50) | | Movie genre |
| duration | INT | NOT NULL | Duration in minutes |
| language | VARCHAR(50) | | Movie language |
| rating | DECIMAL(2,1) | | Movie rating |

## Example

| movie_id | title | genre | duration | language | rating |
|---:|---|---|---:|---|---:|
| 1 | Example Movie | Action | 150 | English | 8.2 |
| 2 | Example Film | Drama | 130 | Hindi | 7.8 |

---

# 8. Table 3 — Theatres

## Purpose

Stores cinema/theatre information.

## Structure

| Column | Data Type | Constraints | Description |
|---|---|---|---|
| theatre_id | INT | PK, AUTO_INCREMENT | Unique theatre ID |
| theatre_name | VARCHAR(100) | NOT NULL | Theatre name |
| location | VARCHAR(150) | | Theatre location |
| total_seats | INT | NOT NULL | Total number of seats |

## Example

| theatre_id | theatre_name | location | total_seats |
|---:|---|---|---:|
| 1 | PVR Cinema | Jaipur | 100 |
| 2 | INOX Cinema | Jaipur | 120 |

---

# 9. Table 4 — Shows

## Purpose

Stores scheduled movie shows.

A show connects a movie with a theatre at a particular date and time.

## Structure

| Column | Data Type | Constraints | Description |
|---|---|---|---|
| show_id | INT | PK, AUTO_INCREMENT | Unique show ID |
| movie_id | INT | FK, NOT NULL | Movie being shown |
| theatre_id | INT | FK, NOT NULL | Theatre where shown |
| show_date | DATE | NOT NULL | Show date |
| show_time | TIME | NOT NULL | Show time |
| ticket_price | DECIMAL(10,2) | NOT NULL | Price per ticket |

## Relationships

```text
movies.movie_id
      │
      └──────> shows.movie_id

theatres.theatre_id
      │
      └──────> shows.theatre_id
```

## Example

| show_id | movie_id | theatre_id | show_date | show_time | ticket_price |
|---:|---:|---:|---|---|---:|
| 1 | 1 | 1 | 2026-08-10 | 14:00 | 200.00 |
| 2 | 2 | 2 | 2026-08-10 | 18:00 | 250.00 |

---

# 10. Table 5 — Seats

## Purpose

Stores the physical seats belonging to each theatre.

## Structure

| Column | Data Type | Constraints | Description |
|---|---|---|---|
| seat_id | INT | PK, AUTO_INCREMENT | Unique seat ID |
| theatre_id | INT | FK, NOT NULL | Theatre containing the seat |
| seat_number | VARCHAR(10) | NOT NULL | Seat label |
| status | VARCHAR(20) | DEFAULT AVAILABLE | General seat status |

## Example

| seat_id | theatre_id | seat_number | status |
|---:|---:|---|---|
| 1 | 1 | A1 | AVAILABLE |
| 2 | 1 | A2 | AVAILABLE |
| 3 | 1 | A3 | AVAILABLE |
| 4 | 1 | A4 | AVAILABLE |

## Important Design Rule

The `seats` table represents the physical seats in a theatre.

**A seat's availability for a specific show must not be treated as a permanent property of the seat.**

For example:

```text
Theatre 1
Seat A1

2:00 PM Show → BOOKED
8:00 PM Show → AVAILABLE
```

Therefore, booking logic must check seat usage for the selected show.

The final implementation should use the booking/show relationship to determine whether a seat is available for a particular show.

---

# 11. Table 6 — Bookings

## Purpose

Stores the main ticket booking record.

## Structure

| Column | Data Type | Constraints | Description |
|---|---|---|---|
| booking_id | INT | PK, AUTO_INCREMENT | Unique booking ID |
| user_id | INT | FK, NOT NULL | User who made booking |
| show_id | INT | FK, NOT NULL | Selected show |
| booking_date | DATETIME | DEFAULT CURRENT_TIMESTAMP | Booking timestamp |
| total_amount | DECIMAL(10,2) | NOT NULL | Total booking amount |
| status | VARCHAR(20) | DEFAULT CONFIRMED | Booking status |

## Status

```text
CONFIRMED
CANCELLED
```

## Relationships

```text
users.user_id
      │
      └──────> bookings.user_id

shows.show_id
      │
      └──────> bookings.show_id
```

---

# 12. Table 7 — Booking Seats

## Purpose

This is a junction table connecting bookings with seats.

A booking can contain multiple seats.

Example:

```text
Booking #101
    │
    ├── A1
    ├── A2
    └── A3
```

## Structure

| Column | Data Type | Constraints | Description |
|---|---|---|---|
| booking_id | INT | PK, FK | Booking |
| seat_id | INT | PK, FK | Selected seat |

## Composite Primary Key

```text
PRIMARY KEY (booking_id, seat_id)
```

This prevents the same seat from being added twice to the same booking.

---

# 13. Relationship Definitions

## Users → Bookings

```text
One User
   ↓
Many Bookings
```

Cardinality:

```text
1 : N
```

---

## Movies → Shows

```text
One Movie
   ↓
Many Shows
```

Cardinality:

```text
1 : N
```

A movie can have multiple shows.

---

## Theatres → Shows

```text
One Theatre
   ↓
Many Shows
```

Cardinality:

```text
1 : N
```

A theatre can host many shows.

---

## Theatres → Seats

```text
One Theatre
   ↓
Many Seats
```

Cardinality:

```text
1 : N
```

---

## Shows → Bookings

```text
One Show
   ↓
Many Bookings
```

Cardinality:

```text
1 : N
```

---

## Bookings → Booking Seats

```text
One Booking
   ↓
Many Booking Seat Records
```

Cardinality:

```text
1 : N
```

---

## Seats → Booking Seats

```text
One Seat
   ↓
Many Booking Seat Records
```

Conceptually, a physical seat can appear in many historical bookings, but it must not be booked twice for the **same show**.

---

# 14. Complete Logical Relationship Model

```text
USERS
  │
  │ 1:N
  ▼
BOOKINGS
  │
  │ 1:N
  ▼
BOOKING_SEATS
  ▲
  │ N:1
  │
SEATS
  ▲
  │ N:1
  │
THEATRES
  │
  │ 1:N
  ▼
SHOWS
  ▲
  │ N:1
  │
MOVIES
```

---

# 15. Planned SQL Schema

The following SQL represents the planned database. It is **not required to be executed yet**.

```sql
CREATE DATABASE IF NOT EXISTS movie_ticket_management;

USE movie_ticket_management;

CREATE TABLE users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL
);

CREATE TABLE movies (
    movie_id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(100) NOT NULL,
    genre VARCHAR(50),
    duration INT NOT NULL,
    language VARCHAR(50),
    rating DECIMAL(2,1)
);

CREATE TABLE theatres (
    theatre_id INT PRIMARY KEY AUTO_INCREMENT,
    theatre_name VARCHAR(100) NOT NULL,
    location VARCHAR(150),
    total_seats INT NOT NULL
);

CREATE TABLE shows (
    show_id INT PRIMARY KEY AUTO_INCREMENT,
    movie_id INT NOT NULL,
    theatre_id INT NOT NULL,
    show_date DATE NOT NULL,
    show_time TIME NOT NULL,
    ticket_price DECIMAL(10,2) NOT NULL,

    FOREIGN KEY (movie_id)
        REFERENCES movies(movie_id),

    FOREIGN KEY (theatre_id)
        REFERENCES theatres(theatre_id)
);

CREATE TABLE seats (
    seat_id INT PRIMARY KEY AUTO_INCREMENT,
    theatre_id INT NOT NULL,
    seat_number VARCHAR(10) NOT NULL,
    status VARCHAR(20) DEFAULT 'AVAILABLE',

    FOREIGN KEY (theatre_id)
        REFERENCES theatres(theatre_id),

    UNIQUE (theatre_id, seat_number)
);

CREATE TABLE bookings (
    booking_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    show_id INT NOT NULL,
    booking_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) DEFAULT 'CONFIRMED',

    FOREIGN KEY (user_id)
        REFERENCES users(user_id),

    FOREIGN KEY (show_id)
        REFERENCES shows(show_id)
);

CREATE TABLE booking_seats (
    booking_id INT NOT NULL,
    seat_id INT NOT NULL,

    PRIMARY KEY (booking_id, seat_id),

    FOREIGN KEY (booking_id)
        REFERENCES bookings(booking_id),

    FOREIGN KEY (seat_id)
        REFERENCES seats(seat_id)
);
```

---

# 16. Future Booking Availability Constraint

When actual booking implementation starts, the application must prevent:

```text
Same Show
+
Same Seat
+
Second Booking
=
NOT ALLOWED
```

The exact database constraint/query strategy will be finalized during JDBC/DAO implementation.

The GUI must not assume that a seat is globally unavailable simply because it was booked for another show.

---

# 17. Sample Data Plan

When the database is implemented, sample data should be added for demonstration.

### Users

```text
admin / ADMIN
user1 / USER
```

### Movies

```text
2–5 movies
```

### Theatres

```text
2–3 theatres
```

### Seats

```text
At least 10–20 seats per demonstration theatre
```

### Shows

```text
Several shows with different dates/times
```

### Bookings

```text
A small number of test bookings
```

---

# 18. Normalization

The planned database separates different types of information into different tables.

For example:

```text
Movie information
    ↓
movies

Theatre information
    ↓
theatres

Show information
    ↓
shows

User information
    ↓
users

Booking information
    ↓
bookings
```

This reduces unnecessary duplication and makes the database easier to maintain.

---

# 19. GUI-to-Database Mapping

The future GUI modules will map to database tables as follows:

| GUI Module | Main Table(s) |
|---|---|
| Login | users |
| Movie Management | movies |
| Theatre Management | theatres |
| Show Management | shows, movies, theatres |
| Seat Management | seats, theatres |
| Ticket Booking | bookings, booking_seats, shows, seats |
| Booking History | bookings, booking_seats, shows |
| Dashboard | Aggregated information |

---

# 20. Java Model Mapping

The planned Java model classes will approximately correspond to:

```text
User.java
Movie.java
Theatre.java
Show.java
Seat.java
Booking.java
BookingSeat.java
```

Example future structure:

```text
model/
├── User.java
├── Movie.java
├── Theatre.java
├── Show.java
├── Seat.java
├── Booking.java
└── BookingSeat.java
```

These classes should be created based on this design when backend development starts.

---

# 21. DAO Mapping

The future DAO layer will approximately contain:

```text
dao/
├── UserDAO.java
├── MovieDAO.java
├── TheatreDAO.java
├── ShowDAO.java
├── SeatDAO.java
├── BookingDAO.java
└── BookingSeatDAO.java
```

The GUI should not contain raw SQL statements.

The intended architecture is:

```text
Swing UI
   ↓
DAO
   ↓
JDBC
   ↓
MySQL
```

---

# 22. Current Implementation Status

At this stage:

```text
Database Design       ✅ Completed
Database Documentation ✅ Completed
Table Planning         ✅ Completed
Relationships          ✅ Planned
SQL Schema             ✅ Prepared
Actual MySQL Database  ⏳ Postponed
JDBC Connection        ⏳ Later
DAO Implementation     ⏳ Later
```

The GUI may therefore be developed using temporary/mock data while keeping the database design as the future integration contract.

---

# 23. Antigravity Development Rule

Antigravity must follow these rules:

1. Do not change the database entities without approval.
2. Do not invent additional database tables unless required by an approved requirement.
3. Do not implement MySQL unless explicitly requested at the current development stage.
4. Build GUI screens using mock/sample data where necessary.
5. Keep field names consistent with this document.
6. Keep Java model names consistent with the planned tables.
7. Keep future DAO boundaries compatible with this design.
8. Do not place SQL directly inside Swing UI classes.
9. Preserve the distinction between a physical seat and its availability for a specific show.
10. When MySQL integration begins, use JDBC and `PreparedStatement`.

---

# 24. Database Design Acceptance Criteria

The database design is considered complete when:

- [x] Database name is defined.
- [x] All required entities are identified.
- [x] Columns are defined.
- [x] Data types are defined.
- [x] Primary keys are defined.
- [x] Foreign keys are defined.
- [x] Relationships are defined.
- [x] Booking structure is defined.
- [x] Seat structure is defined.
- [x] Planned SQL schema is available.
- [x] GUI-to-database mapping is defined.
- [x] Java model mapping is defined.
- [x] DAO mapping is defined.
- [x] Actual database implementation can be postponed.

---

# 25. Next Step

After this document, the next documentation task is:

```text
05-ER-Diagram.md
```

After the ER diagram, prepare:

```text
06-Use-Case.md
07-DFD.md
08-Flowchart.md
09-UI-Specification.md
```

After those documents are ready, GUI development can begin in Antigravity.

The actual MySQL implementation can be introduced later:

```text
GUI Prototype
      ↓
Java Models
      ↓
MySQL Database
      ↓
JDBC
      ↓
DAO
      ↓
Real Data
```

---

# 26. Final Database Design Summary

```text
movie_ticket_management
│
├── users
│     └── Authentication
│
├── movies
│     └── Movie information
│
├── theatres
│     └── Theatre information
│
├── shows
│     └── Movie + Theatre + Date + Time + Price
│
├── seats
│     └── Theatre seats
│
├── bookings
│     └── User + Show + Amount
│
└── booking_seats
      └── Booking + Selected Seats
```

**This document is the database design baseline for the project. Actual MySQL implementation is intentionally postponed so GUI development can begin first.**
