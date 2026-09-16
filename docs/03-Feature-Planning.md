# Feature Planning Document

# Movie Ticket Management System

**Document:** Feature Planning  
**Version:** 1.0  
**Status:** Development Baseline  
**Technology:** Java 21 + Java Swing + MySQL + JDBC + Maven  
**IDE:** IntelliJ IDEA

---

# 1. Purpose

This document converts the requirements defined in the SRS into a practical development plan.

It defines:

- Features
- Modules
- Priorities
- Dependencies
- Development sequence
- Acceptance criteria
- Four-day implementation schedule

The SRS remains the source of truth. Features outside the approved scope should not be added during the four-day development period.

---

# 2. Project Goal

The goal is to develop a functional Movie Ticket Management System that allows authorized users to manage movies, theatres, shows, seats, and ticket bookings through a Java Swing GUI backed by MySQL.

The minimum complete workflow is:

```text
Login
  ↓
Dashboard
  ↓
Select Movie
  ↓
Select Theatre
  ↓
Select Show
  ↓
Select Available Seats
  ↓
Calculate Total
  ↓
Confirm Booking
  ↓
Save Booking
  ↓
View Booking History
```

---

# 3. Feature Priority

Priority levels:

- **P0 — Critical:** Must work for the project to be considered functional.
- **P1 — High:** Required for a complete project demonstration.
- **P2 — Medium:** Important but can be simplified if time is limited.
- **P3 — Future:** Not required for the current four-day version.

---

# 4. Feature Summary

| ID | Feature | Priority | Module | Status |
|---|---|---|---|---|
| F-01 | User Login | P0 | Authentication | Planned |
| F-02 | Role Validation | P0 | Authentication | Planned |
| F-03 | Dashboard | P0 | UI | Planned |
| F-04 | Movie CRUD | P0 | Movie | Planned |
| F-05 | Theatre CRUD | P0 | Theatre | Planned |
| F-06 | Show CRUD | P0 | Show | Planned |
| F-07 | Seat Management | P0 | Seat | Planned |
| F-08 | Seat Selection | P0 | Booking | Planned |
| F-09 | Ticket Booking | P0 | Booking | Planned |
| F-10 | Booking History | P1 | Booking | Planned |
| F-11 | Search | P1 | Management | Planned |
| F-12 | Input Validation | P0 | Utility | Planned |
| F-13 | Error Handling | P0 | Utility | Planned |
| F-14 | Logout | P0 | Authentication | Planned |
| F-15 | GitHub Repository | P0 | Deployment | Planned |
| F-16 | Documentation | P0 | Documentation | Planned |
| F-17 | Online Payment | P3 | Future | Not Planned |
| F-18 | Mobile App | P3 | Future | Not Planned |
| F-19 | Email/SMS | P3 | Future | Not Planned |
| F-20 | AI Recommendations | P3 | Future | Not Planned |

---

# 5. Module 1 — Authentication

## F-01: User Login

### Description

Users shall log into the system using username and password.

### Inputs

```text
Username
Password
```

### Process

```text
Enter Credentials
       ↓
Validate Input
       ↓
Check users Table
       ↓
Credentials Valid?
   /           \
 Yes           No
 ↓              ↓
Dashboard    Error Message
```

### UI Components

- `JLabel`
- `JTextField`
- `JPasswordField`
- `JButton`

### Acceptance Criteria

- Login screen opens successfully.
- Empty credentials are rejected.
- Valid credentials open the dashboard.
- Invalid credentials display an error.
- Password field hides the password.

---

# 6. Module 2 — Role Management

## F-02: Role Validation

The system shall identify the logged-in user's role.

Roles:

```text
ADMIN
USER
```

### Admin

Can access:

```text
Movies
Theatres
Shows
Seats
Bookings
```

### User

Can access:

```text
Movies
Shows
Seat Selection
Booking
Booking History
```

### Acceptance Criteria

- Role is loaded from the database.
- Unauthorized modules are disabled or inaccessible.
- Admin and user dashboards behave according to their permissions.

---

# 7. Module 3 — Dashboard

## F-03: Dashboard

The dashboard is the central navigation screen.

### Required Buttons

```text
+-----------------------------+
| MOVIE TICKET MANAGEMENT     |
+-----------------------------+
| Movies       | Theatres      |
| Shows        | Seats         |
| Book Ticket  | History       |
| Logout       |               |
+-----------------------------+
```

### Acceptance Criteria

- Dashboard opens after successful login.
- Each button opens the correct module.
- Logout returns to login.

---

# 8. Module 4 — Movie Management

## F-04: Movie CRUD

The admin shall manage movie records.

### Fields

```text
Movie ID
Title
Genre
Duration
Language
Rating
```

### Operations

```text
CREATE
READ
UPDATE
DELETE
SEARCH
CLEAR
```

### UI

Use:

- `JTextField`
- `JComboBox` where appropriate
- `JButton`
- `JTable`

### Database

Table:

```text
movies
```

### Acceptance Criteria

- Movie can be added.
- Movie records appear in the table.
- Existing movie can be updated.
- Movie can be deleted when allowed.
- Movie can be searched.
- Invalid input is rejected.

---

# 9. Module 5 — Theatre Management

## F-05: Theatre CRUD

The admin shall manage theatres.

### Fields

```text
Theatre ID
Theatre Name
Location
Total Seats
```

### Operations

```text
CREATE
READ
UPDATE
DELETE
SEARCH
CLEAR
```

### Database

Table:

```text
theatres
```

### Acceptance Criteria

- Theatre can be added.
- Theatre list can be displayed.
- Theatre can be updated.
- Theatre can be deleted when allowed.
- Search works.
- Total seats must be positive.

---

# 10. Module 6 — Show Management

## F-06: Show CRUD

The admin shall manage movie shows.

### Fields

```text
Show ID
Movie
Theatre
Show Date
Show Time
Ticket Price
```

### Relationships

```text
Movie ──────┐
            ├──> Show
Theatre ────┘
```

### Operations

```text
CREATE
READ
UPDATE
DELETE
SEARCH
```

### UI

Movie and theatre should preferably use:

```text
JComboBox
```

### Acceptance Criteria

- Only existing movies can be selected.
- Only existing theatres can be selected.
- Date and time are valid.
- Ticket price is positive.
- Show can be created.
- Show can be updated.
- Show can be deleted when allowed.

---

# 11. Module 7 — Seat Management

## F-07: Seat Management

The system shall manage seats belonging to theatres.

### Fields

```text
Seat ID
Theatre ID
Seat Number
Status
```

### Status

```text
AVAILABLE
BOOKED
```

### Seat Layout

```text
              SCREEN
        -----------------

        [A1] [A2] [A3] [A4]

        [B1] [B2] [B3] [B4]

        [C1] [C2] [C3] [C4]

        [D1] [D2] [D3] [D4]
```

### Important Rule

A seat that is already booked for a particular show must not be selectable for another booking of the same show.

### Acceptance Criteria

- Seats are displayed correctly.
- Available seats can be selected.
- Booked seats cannot be selected.
- Seat information is stored correctly.

---

# 12. Module 8 — Ticket Booking

## F-08: Seat Selection

The user shall select available seats for a show.

### Workflow

```text
Select Movie
     ↓
Select Theatre
     ↓
Select Show
     ↓
Load Seats
     ↓
Select Available Seats
```

### Acceptance Criteria

- Correct show is selected.
- Correct theatre seats are loaded.
- Booked seats are unavailable.
- Multiple seats can be selected.

---

## F-09: Ticket Booking

### Booking Workflow

```text
Select Show
     ↓
Select Seats
     ↓
Read Ticket Price
     ↓
Calculate Total
     ↓
Confirm
     ↓
Create Booking
     ↓
Create Booking-Seats Records
     ↓
Update Seat Availability
```

### Formula

```text
Total Amount =
Number of Selected Seats × Ticket Price
```

### Example

```text
Selected Seats = 3
Ticket Price = ₹200

Total = 3 × 200
      = ₹600
```

### Acceptance Criteria

- Booking cannot be created without a show.
- Booking cannot be created without a seat.
- Duplicate seat booking is prevented.
- Total amount is calculated correctly.
- Booking is stored in MySQL.
- Selected seats are associated with the booking.

---

# 13. Module 9 — Booking History

## F-10: Booking History

Users shall be able to view previous bookings.

### Information

```text
Booking ID
Movie
Theatre
Show Date
Show Time
Seats
Total Amount
Booking Date
Status
```

### Acceptance Criteria

- Booking records are loaded from the database.
- User can see their own booking history.
- Admin can view booking records according to role permissions.

---

# 14. Module 10 — Search

## F-11: Search

Search functionality shall be provided where useful.

### Movie Search

```text
Movie ID
Title
```

### Theatre Search

```text
Theatre ID
Theatre Name
```

### Show Search

```text
Show ID
Movie
Theatre
Date
```

### Booking Search

```text
Booking ID
```

### Acceptance Criteria

- Search returns matching records.
- Empty search can display all records where appropriate.
- Search does not crash on invalid input.

---

# 15. Module 11 — Validation

## F-12: Input Validation

All forms must validate user input before database operations.

### Required Validation

```text
Empty fields
Invalid numbers
Negative numbers
Invalid price
Invalid duration
Invalid date
Invalid seat
Duplicate records
```

### Examples

```text
Movie title cannot be empty.

Duration must be greater than 0.

Ticket price must be greater than 0.

Please select a movie.

Please select a theatre.

Please select at least one seat.
```

---

# 16. Module 12 — Error Handling

## F-13: Error Handling

The application should handle common errors without crashing.

### Database Errors

```text
Unable to connect to database.
Unable to save record.
Unable to update record.
Unable to delete record.
```

### User Errors

```text
Invalid login.
Required field missing.
Invalid value.
Seat already booked.
```

Use appropriate Swing dialogs such as:

```java
JOptionPane.showMessageDialog(...)
```

---

# 17. Module 13 — Logout

## F-14: Logout

The user shall be able to log out.

### Workflow

```text
Dashboard
   ↓
Logout
   ↓
Clear Session
   ↓
Login Screen
```

---

# 18. Module 14 — GitHub

## F-15: GitHub Repository

The project shall be maintained using Git and GitHub.

### Repository

```text
MovieTicketManagementSystem
```

### Important Files

```text
README.md
pom.xml
.gitignore
database/movie_ticket_management.sql
src/
docs/
screenshots/
```

### Recommended Commit Sequence

```text
Initial project setup
Add SRS and documentation
Add database schema
Add Maven configuration
Add JDBC connection
Add model classes
Add DAO classes
Add login
Add dashboard
Add movie management
Add theatre management
Add show management
Add seat management
Add booking
Add booking history
Add validation
Add testing
Update README
Final project submission
```

---

# 19. Module 15 — Documentation

## F-16: Project Documentation

Required documentation:

```text
01-Project-Proposal.md
02-SRS.md
03-Feature-Planning.md
04-Database-Design.md
05-ER-Diagram.md
06-Use-Case.md
07-DFD.md
08-Flowchart.md
09-UI-Specification.md
README.md
```

---

# 20. Feature Dependencies

The features must be implemented in dependency order.

```text
SRS
 ↓
Feature Planning
 ↓
Database Design
 ↓
MySQL Database
 ↓
Maven Project
 ↓
JDBC Connection
 ↓
Model Classes
 ↓
DAO Layer
 ↓
Login
 ↓
Dashboard
 ↓
Movies
 ↓
Theatres
 ↓
Shows
 ↓
Seats
 ↓
Booking
 ↓
Booking History
 ↓
Testing
 ↓
GitHub
```

### Dependency Details

| Feature | Depends On |
|---|---|
| Login | Users table + JDBC |
| Dashboard | Login |
| Movie CRUD | Database + JDBC |
| Theatre CRUD | Database + JDBC |
| Show CRUD | Movies + Theatres |
| Seat Management | Theatres |
| Seat Selection | Shows + Seats |
| Booking | Users + Shows + Seats |
| Booking History | Bookings |
| Testing | All implemented modules |
| GitHub | Project files |

---

# 21. Development Order

Follow this exact order.

## Phase 1 — Planning

```text
SRS                    ✅
Feature Planning       ← Current
Database Design
ER Diagram
Use Case
DFD
Flowchart
UI Specification
```

## Phase 2 — Environment

```text
Java 21
Maven
MySQL
MySQL Workbench
IntelliJ IDEA
Git
GitHub
```

## Phase 3 — Project Foundation

```text
Maven Project
pom.xml
Package Structure
Database Connection
Configuration
```

## Phase 4 — Backend

```text
Model Classes
DAO Classes
Validation
Database Queries
```

## Phase 5 — GUI

```text
Login
Dashboard
Movie Management
Theatre Management
Show Management
Seat Management
Booking
Booking History
```

## Phase 6 — Testing

```text
Unit/Method Testing
GUI Testing
Database Testing
Integration Testing
Booking Testing
```

## Phase 7 — Submission

```text
README
Screenshots
SQL File
Documentation
GitHub
Final Demo
```

---

# 22. Four-Day Schedule

## DAY 1 — Planning + Foundation

### Session 1

```text
Feature Planning
Database Design
ER Diagram
```

### Session 2

```text
Install/check Java 21
Check Maven
Check MySQL
Check IntelliJ IDEA
Check Git
```

### Session 3

```text
Create Maven project
Create packages
Create database
Create tables
Insert sample data
```

### Session 4

```text
Implement DBConnection
Test JDBC
Create Model classes
```

### Day 1 Target

```text
✅ Maven project
✅ Database
✅ Tables
✅ JDBC connection
✅ Models
```

---

# 23. DAY 2 — Core Application

### Session 1

```text
Login GUI
Login DAO
Authentication
```

### Session 2

```text
Dashboard
Movie CRUD
```

### Session 3

```text
Theatre CRUD
Show CRUD
```

### Session 4

```text
Seat management
```

### Day 2 Target

```text
✅ Login
✅ Dashboard
✅ Movies
✅ Theatres
✅ Shows
✅ Seats
```

---

# 24. DAY 3 — Booking + Testing

### Session 1

```text
Seat Selection
```

### Session 2

```text
Ticket Booking
```

### Session 3

```text
Booking History
Validation
Error Handling
```

### Session 4

```text
Complete testing
Fix bugs
```

### Day 3 Target

```text
✅ Booking
✅ Seat selection
✅ Booking history
✅ Validation
✅ Testing
```

---

# 25. DAY 4 — Finalization

### Session 1

```text
Bug fixing
GUI cleanup
Database cleanup
```

### Session 2

```text
README
Screenshots
Documentation
SQL export
```

### Session 3

```text
Git initialization
Git commits
GitHub repository
Push project
```

### Session 4

```text
Final testing
Project demonstration
Prepare viva questions
```

### Day 4 Target

```text
✅ Working project
✅ Documentation
✅ Screenshots
✅ GitHub
✅ Final demo
```

---

# 26. MVP Definition

Because the development time is only four days, the following is the minimum viable project.

```text
                    MVP
                     |
        +------------+------------+
        |            |            |
      Login       Management    Booking
                     |            |
              +------+-----+      |
              |      |     |      |
            Movie Theatre Show   Seats
                     |            |
                     +-----+------+
                           |
                           v
                    Booking History
```

The MVP must work before any optional feature is attempted.

---

# 27. Features NOT to Build During the Four Days

Do not spend development time on:

```text
❌ Online Payment
❌ OTP
❌ Email Service
❌ SMS Service
❌ AI Recommendation
❌ Mobile Application
❌ Cloud Deployment
❌ Movie Streaming
❌ External Movie API
❌ Complex Analytics
❌ Chat System
❌ Social Login
```

These can be mentioned under **Future Scope**.

---

# 28. Definition of Done

A feature is considered complete only when:

```text
Code written
    ↓
Compiles successfully
    ↓
GUI works
    ↓
Database operation works
    ↓
Input validation works
    ↓
Error handling works
    ↓
Feature tested
    ↓
Git commit created
```

Do not mark a feature complete simply because the GUI has been created.

---

# 29. Final Feature Checklist

Before submission, verify:

## Authentication

- [ ] Login works
- [ ] Invalid login handled
- [ ] Role works
- [ ] Logout works

## Movies

- [ ] Add
- [ ] View
- [ ] Update
- [ ] Delete
- [ ] Search

## Theatres

- [ ] Add
- [ ] View
- [ ] Update
- [ ] Delete
- [ ] Search

## Shows

- [ ] Add
- [ ] View
- [ ] Update
- [ ] Delete
- [ ] Search

## Seats

- [ ] Display
- [ ] Select
- [ ] Prevent duplicate booking

## Booking

- [ ] Select show
- [ ] Select seats
- [ ] Calculate total
- [ ] Save booking
- [ ] Save selected seats

## History

- [ ] Display booking history

## Quality

- [ ] Validation
- [ ] Error handling
- [ ] No major crashes
- [ ] Database relationships work
- [ ] Project builds successfully

## Submission

- [ ] README
- [ ] SQL file
- [ ] Documentation
- [ ] Screenshots
- [ ] GitHub repository
- [ ] Final testing

---

# 30. Next Development Step

After completing this feature-planning document, the next document is:

```text
04-Database-Design.md
```

It should define:

```text
Database
   ↓
Tables
   ↓
Columns
   ↓
Data Types
   ↓
Primary Keys
   ↓
Foreign Keys
   ↓
Constraints
   ↓
Relationships
   ↓
Indexes
   ↓
Sample Data
   ↓
Complete SQL Script
```

After the database design is finalized, the SQL script can be created and tested in MySQL before Java coding begins.

---

# 31. Project Rule

> **Do not code a feature until its database requirement, UI requirement, and functional requirement are clear.**

This prevents major rework during the four-day development period.
