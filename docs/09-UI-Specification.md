# UI Specification — Movie Ticket Management System

**Document ID:** 09  
**Version:** 1.0  
**Project:** Movie Ticket Management System  
**Application Type:** Java Swing Desktop Application  
**Purpose:** UI specification and implementation reference for Antigravity

---

## 1. Document Purpose

This document defines the complete user interface requirements for the **Movie Ticket Management System**.

The document will be used as the UI source of truth while developing the application using:

- Java 21
- Java Swing
- MySQL
- JDBC
- Maven
- MVC Architecture
- IntelliJ IDEA
- Antigravity

The UI must follow the requirements defined in the SRS, Feature Planning, Database Design, ER Diagram, Use Case Diagram, DFD, and Flowchart.

> **Important:** Do not add functionality that is not defined in the project documentation.

---

## 2. UI Design Goals

The application UI should be:

- Simple
- Professional
- Clean
- Easy to understand
- Suitable for a college project
- Easy to navigate
- Consistent across all screens
- Suitable for desktop use
- Suitable for demonstration and viva

Avoid unnecessary animations, complicated graphics, and features outside the project scope.

---

## 3. General UI Standards

### 3.1 Window

Recommended default window size:

```text
Width: 1200 px
Height: 700 px
```

Minimum recommended size:

```text
Width: 1000 px
Height: 600 px
```

### 3.2 Font

Recommended font:

```text
Segoe UI
```

Suggested sizes:

| Element | Size |
|---|---:|
| Application title | 24–28 |
| Page title | 20–24 |
| Section heading | 16–18 |
| Normal text | 14 |
| Button text | 14 |
| Table text | 13–14 |
| Error message | 13–14 |

---

## 4. Navigation Structure

```text
                    Login
                      |
          +-----------+-----------+
          |                       |
        ADMIN                  USER
          |                       |
          v                       v
 Admin Dashboard          User Dashboard
```

---

## 5. Application Screens

### Authentication

1. Login
2. Registration

### User

3. User Dashboard
4. Movie List
5. Movie Details
6. Show Selection
7. Seat Selection
8. Booking Confirmation
9. Booking Details
10. Booking History

### Admin

11. Admin Dashboard
12. Movie Management
13. Theatre Management
14. Show Management
15. Seat Management
16. Booking Management

---

# 6. Login Screen

**Screen ID:** UI-01

**Screen Name:** Login

### Purpose

Allows an existing Admin or User to authenticate.

### Components

```text
Application Logo / Title

Username
[____________________________]

Password
[____________________________]

[ LOGIN ]

New User?
[ REGISTER ]

[ EXIT ]
```

### Input Fields

**Username**

- `JTextField`
- Cannot be empty

**Password**

- `JPasswordField`
- Cannot be empty

### Buttons

**Login**

```text
Validate credentials
        ↓
Check database
        ↓
Identify role
        ↓
Admin → Admin Dashboard
User  → User Dashboard
```

**Register**

Navigate to Registration Screen.

**Exit**

Close the application.

### Error Messages

```text
Username cannot be empty.
Password cannot be empty.
Invalid username or password.
Database connection failed.
```

---

# 7. Registration Screen

**Screen ID:** UI-02

**Screen Name:** User Registration

### Purpose

Allows a new customer to create an account.

### Components

```text
Registration

Name
[________________________]

Username
[________________________]

Password
[________________________]

Confirm Password
[________________________]

Email
[________________________]

[ REGISTER ]

[ CLEAR ]

[ BACK TO LOGIN ]
```

### Validation

- Name cannot be empty
- Username cannot be empty
- Username must be unique
- Password cannot be empty
- Confirm password must match
- Email must be valid

### Successful Registration

```text
Registration successful.
```

Then return to Login.

---

# 8. User Dashboard

**Screen ID:** UI-03

**Screen Name:** User Dashboard

### Navigation

- Dashboard
- Movies
- Book Tickets
- Booking History
- Logout

### Layout

```text
+---------------------------------------------------------+
| Movie Ticket Management System          Welcome, User  |
+---------------+-----------------------------------------+
| Dashboard     |          Dashboard Content              |
| Movies        |                                         |
| Book Tickets  |                                         |
| My Bookings   |                                         |
| Logout        |                                         |
+---------------+-----------------------------------------+
```

---

# 9. Movie List Screen

**Screen ID:** UI-04

**Screen Name:** Movies

### Components

```text
Search Movie
[________________________] [ SEARCH ]

Movie Table
------------------------------------------------
ID | Title | Genre | Duration | Language
------------------------------------------------
```

### Actions

- View movies
- Search movies
- Select a movie
- Open movie details

---

# 10. Movie Details Screen

**Screen ID:** UI-05

**Screen Name:** Movie Details

### Components

```text
Movie Title
Genre
Language
Duration
Description

[ VIEW SHOWS ]

[ BACK ]
```

`VIEW SHOWS` opens available show selection.

---

# 11. Show Selection Screen

**Screen ID:** UI-06

**Screen Name:** Select Theatre and Show

### Components

```text
Movie: <Movie Name>

Theatre:
[ Select Theatre ▼ ]

Date:
[ Select Date ▼ ]

Available Shows:

-----------------------------------------
Show Time       Theatre       Action
-----------------------------------------
10:00 AM        Screen 1      [SELECT]
01:00 PM        Screen 2      [SELECT]
06:00 PM        Screen 1      [SELECT]
-----------------------------------------

[ BACK ]
```

### Actions

User selects:

1. Theatre
2. Date
3. Show

Then proceeds to Seat Selection.

---

# 12. Seat Selection Screen

**Screen ID:** UI-07

**Screen Name:** Select Seats

### Layout

```text
                 SCREEN
        ---------------------

       [A1] [A2] [A3] [A4] [A5]

       [B1] [B2] [B3] [B4] [B5]

       [C1] [C2] [C3] [C4] [C5]

       [D1] [D2] [D3] [D4] [D5]

Selected Seats:
A2, A3

Total Amount:
₹300

[ CONFIRM BOOKING ]
[ BACK ]
```

### Seat States

- Available
- Selected
- Booked

Use Swing buttons for individual seats.

### Validation

A user cannot select an already booked seat.

If no seat is selected:

```text
Please select at least one seat.
```

---

# 13. Booking Confirmation Screen

**Screen ID:** UI-08

**Screen Name:** Booking Confirmation

### Components

```text
Booking Confirmation

Movie:
Example Movie

Theatre:
Screen 1

Show:
06:00 PM

Date:
DD/MM/YYYY

Selected Seats:
A2, A3

Number of Tickets:
2

Total Amount:
₹300

[ CONFIRM BOOKING ]

[ CANCEL ]
```

### Action

```text
Save booking
      ↓
Update seat status
      ↓
Generate booking details
      ↓
Display confirmation
```

---

# 14. Booking Details Screen

**Screen ID:** UI-09

**Screen Name:** Booking Details

### Components

```text
Booking ID
Movie
Theatre
Show
Date
Seats
Number of Tickets
Total Amount
Booking Status
```

Example:

```text
Booking ID: B001
Movie: Example Movie
Theatre: Screen 1
Show: 06:00 PM
Seats: A2, A3
Total: ₹300
Status: Confirmed
```

---

# 15. Booking History Screen

**Screen ID:** UI-10

**Screen Name:** Booking History

### Table

```text
----------------------------------------------------------------
Booking ID | Movie | Theatre | Date | Seats | Amount | Status
----------------------------------------------------------------
B001       | Movie | Screen1 | ...  | A1,A2 | ₹300   | Confirmed
B002       | Movie | Screen2 | ...  | B1    | ₹150   | Cancelled
----------------------------------------------------------------
```

### Actions

- View booking details
- Cancel an eligible booking

---

# 16. Admin Dashboard

**Screen ID:** UI-11

**Screen Name:** Admin Dashboard

### Navigation

- Dashboard
- Movies
- Theatres
- Shows
- Seats
- Bookings
- Logout

### Layout

```text
+---------------------------------------------------------+
| Movie Ticket Management System                 ADMIN   |
+----------------+----------------------------------------+
| Dashboard      |          Admin Dashboard               |
| Movies         |                                        |
| Theatres       |                                        |
| Shows          |                                        |
| Seats          |                                        |
| Bookings       |                                        |
| Logout         |                                        |
+----------------+----------------------------------------+
```

---

# 17. Movie Management Screen

**Screen ID:** UI-12

**Screen Name:** Manage Movies

### Components

```text
Movie ID
[____________]

Title
[____________]

Genre
[____________]

Language
[____________]

Duration
[____________]

Description
[____________________________]

[ ADD ]
[ UPDATE ]
[ DELETE ]
[ CLEAR ]
```

### Actions

- Add movie
- View movies
- Update movie
- Delete movie

---

# 18. Theatre Management Screen

**Screen ID:** UI-13

**Screen Name:** Manage Theatres

### Components

```text
Theatre ID
[____________]

Theatre Name
[____________]

Location
[____________]

[ ADD ]
[ UPDATE ]
[ DELETE ]
[ CLEAR ]
```

### Table

```text
--------------------------------------------
ID | Theatre Name | Location
--------------------------------------------
```

---

# 19. Show Management Screen

**Screen ID:** UI-14

**Screen Name:** Manage Shows

### Components

```text
Movie
[ Select Movie ▼ ]

Theatre
[ Select Theatre ▼ ]

Date
[ Select Date ]

Time
[ Select Time ]

[ ADD ]
[ UPDATE ]
[ DELETE ]
[ CLEAR ]
```

### Table

```text
---------------------------------------------------------------
ID | Movie | Theatre | Date | Time
---------------------------------------------------------------
```

---

# 20. Seat Management Screen

**Screen ID:** UI-15

**Screen Name:** Manage Seats

### Components

```text
Theatre
[ Select Theatre ▼ ]

Screen
[ Select Screen ▼ ]

Seat Number
[____________]

Seat Status
[ Available ▼ ]

[ ADD ]
[ UPDATE ]
[ CLEAR ]
```

### Table

```text
--------------------------------------------
Seat ID | Seat Number | Theatre | Status
--------------------------------------------
```

---

# 21. Booking Management Screen

**Screen ID:** UI-16

**Screen Name:** Manage Bookings

### Components

```text
Search Booking
[________________] [ SEARCH ]

Booking Table

-----------------------------------------------------------------
Booking ID | User | Movie | Theatre | Seats | Amount | Status
-----------------------------------------------------------------
```

### Actions

- View bookings
- View booking details
- Manage booking status

---

# 22. Logout

Logout must be available to both roles.

### User

```text
User Dashboard
     ↓
Logout
     ↓
Login Screen
```

### Admin

```text
Admin Dashboard
     ↓
Logout
     ↓
Login Screen
```

Optional confirmation:

```text
Are you sure you want to logout?
[ YES ] [ NO ]
```

---

# 23. UI Navigation Map

## User Navigation

```text
Login
 │
 ├── Register
 │      └── Login
 │
 └── User Dashboard
        │
        ├── Movies
        │    └── Movie Details
        │          └── Available Shows
        │                 └── Seat Selection
        │                        └── Booking Confirmation
        │                               └── Booking Details
        │
        ├── Booking History
        │
        └── Logout
               │
               ▼
             Login
```

## Admin Navigation

```text
Login
 │
 └── Admin Dashboard
        │
        ├── Movie Management
        ├── Theatre Management
        ├── Show Management
        ├── Seat Management
        ├── Booking Management
        └── Logout
               │
               ▼
             Login
```

---

# 24. UI Validation Rules

All forms must validate user input before database operations.

- Required fields cannot be empty.
- Numeric fields must contain valid numbers.
- IDs must be valid.
- Duplicate records should be prevented where required.
- Invalid input should show a clear error message.
- Database errors should not crash the application.

### Success

```text
Operation completed successfully.
```

### Error

```text
Please enter all required fields.
```

### Database Error

```text
Unable to connect to the database.
Please try again.
```

### Confirmation

```text
Are you sure you want to delete this record?
```

---

# 25. UI Error Handling

The GUI must never expose raw Java exceptions to the user.

Do not display raw exceptions such as:

```text
java.sql.SQLException: ...
```

Instead display:

```text
Unable to complete the operation.
Please try again.
```

Technical errors should be logged for debugging.

---

# 26. GUI Architecture

The application should follow MVC architecture.

```text
User Interface
      ↓
Controllers
      ↓
DAO Layer
      ↓
JDBC
      ↓
MySQL Database
```

Recommended structure:

```text
src/
└── main/
    └── java/
        └── com/
            └── movieticket/
                ├── model/
                ├── dao/
                ├── controller/
                ├── view/
                ├── util/
                └── Main.java
```

---

# 27. Swing Components

| Requirement | Swing Component |
|---|---|
| Text input | `JTextField` |
| Password | `JPasswordField` |
| Button | `JButton` |
| Label | `JLabel` |
| Dropdown | `JComboBox` |
| Table | `JTable` |
| Scroll | `JScrollPane` |
| Checkbox | `JCheckBox` |
| Radio option | `JRadioButton` |
| Main container | `JPanel` |
| Window | `JFrame` |
| Dialog | `JOptionPane` |

---

# 28. UI Consistency

Every screen must maintain:

- Same application title
- Same font family
- Same button style
- Same spacing
- Same navigation style
- Same error-message format
- Same window behavior

Do not create completely different designs for different screens.

---

# 29. Database Integration

The GUI must not directly execute SQL queries.

Use:

```text
Swing View
     ↓
Controller
     ↓
DAO
     ↓
JDBC
     ↓
MySQL
```

Example:

```text
LoginFrame
    ↓
LoginController
    ↓
UserDAO
    ↓
DatabaseConnection
    ↓
MySQL
```

---

# 30. UI Security Requirements

The application should:

- Never display passwords.
- Use `JPasswordField` for passwords.
- Avoid storing passwords unnecessarily in UI variables.
- Validate login credentials through the database.
- Separate Admin and User access.
- Prevent users from opening Admin functionality directly.

---

# 31. Implementation Rules for Antigravity

Antigravity must follow these rules:

1. Use Java 21.
2. Use Java Swing.
3. Use Maven.
4. Use MVC architecture.
5. Use MySQL through JDBC.
6. Follow this UI specification.
7. Do not invent additional features.
8. Do not change database requirements without approval.
9. Do not place SQL directly inside Swing forms.
10. Keep database operations in DAO classes.
11. Keep business logic outside UI classes.
12. Keep UI classes responsible primarily for presentation and user interaction.
13. Use reusable components where appropriate.
14. Display user-friendly error messages.
15. Keep the application runnable after every major implementation step.
16. Do not create unnecessary dependencies.
17. Do not modify the project scope.

---

# 32. Development Order

Implement the GUI in this order:

```text
1. Project Setup
       ↓
2. Database Connection
       ↓
3. Login
       ↓
4. Registration
       ↓
5. Admin Dashboard
       ↓
6. User Dashboard
       ↓
7. Movie Management
       ↓
8. Theatre Management
       ↓
9. Show Management
       ↓
10. Seat Management
       ↓
11. Movie Browsing
       ↓
12. Show Selection
       ↓
13. Seat Selection
       ↓
14. Ticket Booking
       ↓
15. Booking Details
       ↓
16. Booking History
       ↓
17. Booking Management
       ↓
18. Logout
       ↓
19. Testing
```

---

# 33. Acceptance Criteria

The UI will be considered complete when:

- [ ] Login screen works.
- [ ] Registration works.
- [ ] Admin can access Admin Dashboard.
- [ ] User can access User Dashboard.
- [ ] Admin can manage movies.
- [ ] Admin can manage theatres.
- [ ] Admin can manage shows.
- [ ] Admin can manage seats.
- [ ] Admin can view/manage bookings.
- [ ] User can browse movies.
- [ ] User can search movies.
- [ ] User can view movie details.
- [ ] User can select theatre/show.
- [ ] User can select available seats.
- [ ] User can book tickets.
- [ ] User can view booking details.
- [ ] User can view booking history.
- [ ] User can cancel eligible bookings.
- [ ] Logout works.
- [ ] Validation works.
- [ ] Error messages are user-friendly.
- [ ] UI is consistent across screens.
- [ ] No SQL is directly written inside Swing UI classes.
- [ ] Application follows MVC architecture.

---

# 34. Final UI Principle

The UI must prioritize:

```text
Simplicity
    +
Consistency
    +
Usability
    +
Correct Functionality
    +
Clean Architecture
```

The implementation must follow the project's **SRS and database design as the ultimate source of truth**.

---

## Document Status

```text
Document: UI Specification
Version: 1.0
Status: Ready for Implementation
Next Step: Java Maven Project Setup + MVC Structure
```
