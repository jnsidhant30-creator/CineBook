# Software Requirements Specification (SRS)

## Movie Ticket Management System

**Version:** 1.0  
**Application:** Java Swing Desktop Application  
**Language:** Java 21  
**Database:** MySQL  
**Connectivity:** JDBC  
**Build Tool:** Maven  
**IDE:** IntelliJ IDEA

---

## 1. Introduction

### 1.1 Purpose

This Software Requirements Specification defines the functional and non-functional requirements of the **Movie Ticket Management System**. It is the primary reference for development, GUI design, database design, testing, documentation, and project evaluation.

### 1.2 Project Description

The Movie Ticket Management System is a Java-based desktop application for managing movies, theatres, shows, seats, and ticket bookings. Java Swing will provide the graphical interface, while MySQL will store persistent data and JDBC will connect the application to the database.

### 1.3 Problem Statement

Manual movie ticket management can cause duplicate records, booking errors, difficulty tracking seats, inefficient searching, and difficulty maintaining movie and show information. The proposed system provides a centralized computerized solution.

---

## 2. Objectives

1. Develop a user-friendly movie ticket management application.
2. Use Java 21 for application development.
3. Use Java Swing for GUI development.
4. Use MySQL for data storage.
5. Use JDBC for database connectivity.
6. Manage movie, theatre, show, seat, and booking information.
7. Demonstrate CRUD operations.
8. Provide basic ticket booking and booking history.
9. Implement input validation and error handling.
10. Demonstrate practical Java, GUI, and database concepts.

---

## 3. Scope

### 3.1 In Scope

- User login
- Dashboard
- Movie management
- Theatre management
- Show management
- Seat management
- Ticket booking
- Booking history
- MySQL database
- JDBC connectivity
- CRUD operations
- Input validation
- Java Swing GUI

### 3.2 Out of Scope

- Online payment gateway
- Real-time payment processing
- AI recommendations
- Online streaming
- Mobile application
- SMS/email notifications
- Cloud deployment
- Third-party movie APIs
- Advanced analytics

These may be considered future enhancements.

---

## 4. Users and Roles

### 4.1 Admin

The admin can:

- Login
- Manage movies
- Manage theatres
- Manage shows
- Manage seats
- View bookings
- Logout

### 4.2 Booking User

The booking user can:

- Login
- View movies
- View shows
- Select seats
- Book tickets
- View booking history
- Logout

### 4.3 Permission Matrix

| Function | Admin | User |
|---|:---:|:---:|
| Login | Yes | Yes |
| Dashboard | Yes | Yes |
| View Movies | Yes | Yes |
| Add/Update/Delete Movies | Yes | No |
| Manage Theatres | Yes | No |
| Manage Shows | Yes | No |
| Manage Seats | Yes | No |
| Book Ticket | Yes | Yes |
| Booking History | Yes | Yes |
| Logout | Yes | Yes |

---

## 5. Functional Requirements

### FR-01: Login

The system shall provide username and password fields and validate credentials against the user database.

Valid login:

```text
Login -> Validate -> Dashboard
```

Invalid login:

```text
Login -> Validate -> Error Message
```

### FR-02: Dashboard

The dashboard shall provide navigation to:

- Movies
- Theatres
- Shows
- Seats
- Book Ticket
- Booking History
- Logout

### FR-03: Movie Management

The system shall support adding, viewing, updating, deleting, searching, and clearing movie records.

Movie fields:

| Field | Description |
|---|---|
| Movie ID | Unique identifier |
| Title | Movie title |
| Genre | Movie category |
| Duration | Duration in minutes |
| Language | Movie language |
| Rating | Movie rating |

### FR-04: Theatre Management

The system shall support adding, viewing, updating, deleting, and searching theatre records.

Fields:

- Theatre ID
- Theatre Name
- Location
- Total Seats

### FR-05: Show Management

The system shall allow authorized users to manage shows.

Fields:

- Show ID
- Movie
- Theatre
- Show Date
- Show Time
- Ticket Price

Operations:

- Add
- View
- Update
- Delete
- Search

### FR-06: Seat Management

The system shall maintain seats associated with theatres.

Each seat shall contain:

- Seat ID
- Theatre ID
- Seat Number
- Seat Status

Possible statuses:

```text
AVAILABLE
BOOKED
```

### FR-07: Seat Selection

The system shall display available seats for a selected show.

Example:

```text
             SCREEN
       -----------------

       [A1] [A2] [A3] [A4]
       [B1] [B2] [B3] [B4]
       [C1] [C2] [C3] [C4]
       [D1] [D2] [D3] [D4]
```

Already booked seats must not be selectable.

### FR-08: Ticket Booking

The user shall select a movie, theatre, show, and available seats.

Total amount:

```text
Total Amount = Number of Selected Seats × Ticket Price
```

The system shall save the booking after confirmation.

### FR-09: Booking Management

Booking information shall include:

- Booking ID
- User ID
- Show ID
- Booking Date
- Total Amount
- Booking Status

Possible statuses:

```text
CONFIRMED
CANCELLED
```

### FR-10: Booking History

The system shall display:

- Booking ID
- Movie
- Theatre
- Show Date
- Show Time
- Seats
- Total Amount
- Booking Date
- Status

### FR-11: Search

The system shall provide appropriate search functionality for movie, theatre, show, and booking records.

### FR-12: Logout

Logout shall terminate the current session and return the user to the login screen.

---

## 6. CRUD Requirements

### Create

- Add movie
- Add theatre
- Add show
- Create booking

### Read

- View movies
- View theatres
- View shows
- View bookings

### Update

- Update movie
- Update theatre
- Update show

### Delete

- Delete movie
- Delete theatre
- Delete show

---

## 7. Non-Functional Requirements

### NFR-01: Usability

The GUI should be simple, consistent, and easy to understand.

### NFR-02: Performance

Normal CRUD and search operations should respond within a reasonable time.

### NFR-03: Reliability

The system should handle invalid input and database failures without unexpected termination.

### NFR-04: Maintainability

Code should be separated into logical packages such as:

```text
com.movieticket
├── model
├── dao
├── db
├── ui
└── util
```

### NFR-05: Security

- Validate user input.
- Use `PreparedStatement`.
- Protect database credentials.
- Restrict admin operations to authorized users.

### NFR-06: Portability

The application should run on systems with the required Java, MySQL, Maven, and project dependencies.

---

## 8. Technology Stack

| Technology | Purpose |
|---|---|
| Java 21 | Application development |
| Java Swing | GUI |
| JDBC | Database connectivity |
| MySQL | Database |
| Maven | Build/dependency management |
| IntelliJ IDEA | Development |
| Git | Version control |
| GitHub | Repository |

---

## 9. System Architecture

The project will use a layered/MVC-style architecture.

```text
+--------------------------+
|       Swing UI Layer     |
+------------+-------------+
             |
             v
+--------------------------+
|   Application/Logic      |
+------------+-------------+
             |
             v
+--------------------------+
|       DAO Layer          |
+------------+-------------+
             |
             v
+--------------------------+
|          JDBC            |
+------------+-------------+
             |
             v
+--------------------------+
|       MySQL Database     |
+--------------------------+
```

---

## 10. Proposed Project Structure

```text
MovieTicketManagementSystem/
├── pom.xml
├── README.md
├── .gitignore
├── database/
│   └── movie_ticket_management.sql
├── docs/
│   ├── 01-Project-Proposal.md
│   ├── 02-SRS.md
│   ├── 03-Feature-Planning.md
│   ├── 04-Database-Design.md
│   ├── 05-ER-Diagram.md
│   ├── 06-Use-Case.md
│   ├── 07-DFD.md
│   ├── 08-Flowchart.md
│   └── 09-UI-Specification.md
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── movieticket/
        │           ├── Main.java
        │           ├── model/
        │           ├── dao/
        │           ├── db/
        │           ├── ui/
        │           └── util/
        └── resources/
```

---

## 11. Database Requirements

### Database Name

```text
movie_ticket_management
```

### Tables

```text
users
movies
theatres
shows
seats
bookings
booking_seats
```

### Users

| Field | Type | Key |
|---|---|---|
| user_id | INT | PK |
| username | VARCHAR | UNIQUE |
| password | VARCHAR | |
| role | VARCHAR | |

### Movies

| Field | Type | Key |
|---|---|---|
| movie_id | INT | PK |
| title | VARCHAR | |
| genre | VARCHAR | |
| duration | INT | |
| language | VARCHAR | |
| rating | DECIMAL | |

### Theatres

| Field | Type | Key |
|---|---|---|
| theatre_id | INT | PK |
| theatre_name | VARCHAR | |
| location | VARCHAR | |
| total_seats | INT | |

### Shows

| Field | Type | Key |
|---|---|---|
| show_id | INT | PK |
| movie_id | INT | FK |
| theatre_id | INT | FK |
| show_date | DATE | |
| show_time | TIME | |
| ticket_price | DECIMAL | |

### Seats

| Field | Type | Key |
|---|---|---|
| seat_id | INT | PK |
| theatre_id | INT | FK |
| seat_number | VARCHAR | |
| status | VARCHAR | |

### Bookings

| Field | Type | Key |
|---|---|---|
| booking_id | INT | PK |
| user_id | INT | FK |
| show_id | INT | FK |
| booking_date | DATETIME | |
| total_amount | DECIMAL | |
| status | VARCHAR | |

### Booking Seats

| Field | Type | Key |
|---|---|---|
| booking_id | INT | PK/FK |
| seat_id | INT | PK/FK |

---

## 12. Database Relationships

```text
USER
  |
  | 1:N
  v
BOOKING
  |
  | 1:N
  v
BOOKING_SEATS
  |
  | N:1
  v
SEAT

MOVIE
  |
  | 1:N
  v
SHOW
  |
  | N:1
  v
THEATRE

THEATRE
  |
  | 1:N
  v
SEAT
```

---

## 13. GUI Requirements

### 13.1 Login Screen

Components:

- JFrame
- JLabel
- JTextField
- JPasswordField
- JButton

Fields:

- Username
- Password

Buttons:

- Login
- Exit

### 13.2 Dashboard

Buttons/modules:

```text
Movies
Theatres
Shows
Seats
Book Ticket
Booking History
Logout
```

### 13.3 Movie Screen

Fields:

```text
Movie ID
Title
Genre
Duration
Language
Rating
```

Buttons:

```text
Add
Update
Delete
Search
Clear
```

A `JTable` shall display records.

### 13.4 Theatre Screen

Fields:

```text
Theatre ID
Theatre Name
Location
Total Seats
```

Buttons:

```text
Add
Update
Delete
Search
Clear
```

### 13.5 Show Screen

Fields:

```text
Show ID
Movie
Theatre
Show Date
Show Time
Ticket Price
```

### 13.6 Seat Screen

The screen shall display seats and their availability.

### 13.7 Booking Screen

Fields:

```text
Movie
Theatre
Show Date
Show Time
Available Seats
Selected Seats
Ticket Price
Total Amount
```

Button:

```text
Confirm Booking
```

### 13.8 Booking History

A table shall display booking records.

---

## 14. Input Validation

The application shall validate:

- Empty username/password
- Required fields
- Positive duration
- Positive ticket price
- Valid numeric input
- Valid dates
- Seat availability
- Duplicate records where applicable

Example messages:

```text
Username cannot be empty.
Password cannot be empty.
Please enter all required fields.
Duration must be a positive number.
Ticket price must be greater than zero.
Seat is already booked.
```

---

## 15. Error Handling

The application should handle:

```text
Database connection failed.
Invalid username or password.
Movie not found.
Theatre not found.
Show not found.
Seat is already booked.
Unable to save record.
Unable to update record.
Unable to delete record.
```

The application should not terminate unexpectedly because of normal user input errors.

---

## 16. Security Requirements

1. Use `PreparedStatement` for SQL operations.
2. Validate user input.
3. Do not commit real database passwords to GitHub.
4. Use `.gitignore` for sensitive local configuration.
5. Restrict admin functions based on user role.
6. Avoid exposing unnecessary sensitive information.

---

## 17. System Workflow

```text
START
  |
  v
LOGIN
  |
  v
Validate Credentials
  |
  +---- Invalid ----> Error ----> LOGIN
  |
 Valid
  |
  v
DASHBOARD
  |
  +---- Movies
  |
  +---- Theatres
  |
  +---- Shows
  |
  +---- Seats
  |
  +---- Book Ticket
  |         |
  |         v
  |    Select Movie
  |         |
  |         v
  |    Select Theatre
  |         |
  |         v
  |     Select Show
  |         |
  |         v
  |     Select Seats
  |         |
  |         v
  |    Calculate Amount
  |         |
  |         v
  |    Confirm Booking
  |         |
  |         v
  |    Save to Database
  |
  +---- Booking History
  |
  v
LOGOUT
  |
  v
END
```

---

## 18. Use Cases

### Admin

```text
Login
View Dashboard
Manage Movies
Manage Theatres
Manage Shows
Manage Seats
View Bookings
Logout
```

### Booking User

```text
Login
View Dashboard
View Movies
View Shows
Select Seats
Book Ticket
View Booking History
Logout
```

### Use Case Table

| Use Case | Actor | Description |
|---|---|---|
| Login | Admin/User | Authenticate user |
| Dashboard | Admin/User | Main navigation |
| Manage Movies | Admin | CRUD movies |
| Manage Theatres | Admin | CRUD theatres |
| Manage Shows | Admin | CRUD shows |
| Manage Seats | Admin | Manage seats |
| View Movies | User | View movies |
| View Shows | User | View shows |
| Book Ticket | User | Select seats and book |
| Booking History | User/Admin | View bookings |
| Logout | Admin/User | End session |

---

## 19. DFD

### Level 0

```text
+--------+        +-----------------------------+        +---------+
|  User  | -----> | Movie Ticket Management    | -----> |  MySQL  |
|        | <----- | System                      | <----- | Database|
+--------+        +-----------------------------+        +---------+
```

### Level 1

```text
User
 |
 +--> Authentication ------> Users
 |
 +--> Movie Management ----> Movies
 |
 +--> Theatre Management --> Theatres
 |
 +--> Show Management ----> Shows
 |
 +--> Seat Management ----> Seats
 |
 +--> Ticket Booking -----> Bookings
 |                             |
 |                             v
 |                        Booking Seats
 |
 +--> Booking History ----> Bookings
```

---

## 20. Constraints

1. Java 21 is required.
2. Java Swing is required for the GUI.
3. MySQL is required for database storage.
4. JDBC is required for connectivity.
5. Maven is used for project management.
6. MySQL must be running for database operations.
7. The current project focuses on basic ticket management.
8. Advanced online services are outside the current scope.

---

## 21. Assumptions

1. Java 21 is installed.
2. MySQL Server is installed and running.
3. Required Maven dependencies are available.
4. Users have valid credentials.
5. Authorized users enter movie and theatre information.
6. The application uses a local/configured MySQL database.
7. The demonstration database contains a manageable number of records.

---

## 22. Hardware Requirements

### Minimum

```text
Processor : Intel Core i3 or equivalent
RAM       : 4 GB
Storage   : 2 GB available
Display   : 1366 × 768
Keyboard  : Required
Mouse     : Required
```

### Recommended

```text
Processor : Intel Core i5 or equivalent
RAM       : 8 GB
Storage   : SSD
```

---

## 23. Software Requirements

```text
Operating System : Windows / Linux
JDK              : Java 21
IDE              : IntelliJ IDEA
Database         : MySQL
Database Tool    : MySQL Workbench
Build Tool       : Maven
Version Control  : Git
Repository       : GitHub
```

---

## 24. Development Roadmap

```text
Requirement Analysis
        |
        v
SRS
        |
        v
Feature Planning
        |
        v
Database Design
        |
        v
ER Diagram
        |
        v
GUI Design
        |
        v
Maven Project Setup
        |
        v
Model Classes
        |
        v
Database Connection
        |
        v
DAO Layer
        |
        v
Login
        |
        v
Dashboard
        |
        v
Movie CRUD
        |
        v
Theatre CRUD
        |
        v
Show Management
        |
        v
Seat Management
        |
        v
Booking
        |
        v
Testing
        |
        v
Documentation
        |
        v
GitHub
```

---

## 25. Testing Requirements

### Login

- Valid credentials
- Invalid password
- Invalid username
- Empty username
- Empty password

### Movies

- Add movie
- View movies
- Update movie
- Delete movie
- Search movie
- Invalid input

### Theatres

- Add theatre
- View theatres
- Update theatre
- Delete theatre
- Search theatre

### Shows

- Add show
- View shows
- Update show
- Delete show
- Invalid movie/theatre
- Invalid date
- Invalid price

### Booking

- Select available seat
- Select multiple seats
- Calculate total
- Confirm booking
- Prevent duplicate seat booking
- View booking history

---

## 26. Acceptance Criteria

The project will be considered acceptable when:

- The application starts successfully.
- Login works.
- Dashboard works.
- GUI screens are functional.
- MySQL database is created successfully.
- JDBC connection works.
- Movie CRUD works.
- Theatre management works.
- Show management works.
- Seat selection works.
- Basic booking works.
- Booking data is stored in MySQL.
- Booking history works.
- Input validation is implemented.
- Major errors are handled.
- Documentation matches implementation.

---

## 27. Mid-Term I Deliverables

The first mid-term should demonstrate:

### Documentation

- Project Proposal
- SRS
- Feature Planning
- Flowchart
- Database Design
- ER Diagram
- Use Case Diagram
- DFD

### GUI Prototype

- Login
- Dashboard
- Movie screen
- Theatre screen
- Show screen
- Seat screen
- Booking screen

### Database

- MySQL database
- Tables
- Primary keys
- Foreign keys
- Relationships

### Technical Foundation

- Java 21
- Maven project
- Java Swing
- JDBC
- Basic CRUD

---

## 28. Future Scope

Future versions may include:

1. Online payment integration.
2. QR-code tickets.
3. PDF ticket generation.
4. Email confirmation.
5. SMS notifications.
6. Mobile application.
7. Web application.
8. Cloud database.
9. Advanced reports.
10. Movie recommendation system.
11. External movie APIs.
12. Customer reviews and ratings.

These features are not required for the current Mid-Term I scope.

---

## 29. Risks and Mitigation

| Risk | Impact | Mitigation |
|---|---|---|
| MySQL connection failure | High | Test JDBC early |
| Incorrect relationships | High | Finalize ER diagram first |
| GUI errors | Medium | Build incrementally |
| SQL errors | High | Test queries separately |
| Duplicate booking | High | Validate seat availability |
| Invalid input | Medium | Implement validation |
| Code inconsistency | Medium | Follow architecture |
| Lost files | High | Use Git/GitHub |

---

## 30. GitHub Repository Structure

```text
MovieTicketManagementSystem/
├── .gitignore
├── README.md
├── pom.xml
├── database/
│   └── movie_ticket_management.sql
├── docs/
│   ├── 01-Project-Proposal.md
│   ├── 02-SRS.md
│   ├── 03-Feature-Planning.md
│   ├── 04-Database-Design.md
│   ├── 05-ER-Diagram.md
│   ├── 06-Use-Case.md
│   ├── 07-DFD.md
│   ├── 08-Flowchart.md
│   └── 09-UI-Specification.md
├── screenshots/
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── movieticket/
        │           ├── Main.java
        │           ├── model/
        │           ├── dao/
        │           ├── db/
        │           ├── ui/
        │           └── util/
        └── resources/
```

---

## 31. Development Rules

1. This SRS is the functional source of truth.
2. Database design must remain consistent with the SRS.
3. ER diagrams must match the database.
4. GUI screens must implement approved requirements.
5. Database operations must be separated from GUI code.
6. JDBC must be used for MySQL connectivity.
7. Use `PreparedStatement` for SQL operations.
8. Do not remove working functionality without approval.
9. Do not add unapproved features.
10. Advanced features are not required for Mid-Term I.
11. Test major changes before committing.
12. Update documentation when approved requirements change.
13. Keep the project suitable for a college-level minor project.

---

## 32. Conclusion

The Movie Ticket Management System will provide a structured desktop-based solution for managing movies, theatres, shows, seats, and ticket bookings.

The project uses Java 21, Java Swing, JDBC, MySQL, Maven, and IntelliJ IDEA. It demonstrates GUI development, Java programming, database connectivity, CRUD operations, relational database design, validation, testing, documentation, and Git/GitHub version control.

The initial priority is to complete the project proposal, SRS, feature planning, database design, ER diagram, GUI prototype, and flowchart for Mid-Term I. Implementation can then proceed incrementally through JDBC, models, DAO classes, CRUD modules, booking, testing, and GitHub deployment.

---

## 33. Document Control

| Item | Value |
|---|---|
| Document | Software Requirements Specification |
| Project | Movie Ticket Management System |
| Version | 1.0 |
| Status | Development Baseline |
| Language | Java 21 |
| GUI | Java Swing |
| Database | MySQL |
| Connectivity | JDBC |
| Build Tool | Maven |
| IDE | IntelliJ IDEA |
| Primary Use | Development and Mid-Term I Evaluation |
