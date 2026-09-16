CINEBOOK – FEATURE 18
SMART SEAT HOLD + REAL-TIME AVAILABILITY

IMPORTANT:
Do NOT redesign the complete CineBook application.
Do NOT remove existing functionality.
Do NOT modify unrelated modules.

Preserve all existing:

Login
Authentication
Movies
OMDb integration
Movie synchronization
Upcoming Movie Monitor
Notifications
Email
Favorites
Recommendations
City discovery
Theatre
Screen
Show scheduling
Seat selection
Seat categories
Pricing
Booking
Cancellation
QR tickets
Admin dashboard
User dashboard

First inspect the existing Seat, Show, Booking, Booking DAO, database schema and seat-selection UI.

Use existing MVC + DAO architecture.

==================================================
1. TEMPORARY SEAT HOLD
==================================================

When a user selects an available seat:

Change its temporary state to:

HELD

The seat should remain held for a limited period.

Default hold duration:

5 minutes

If an existing configurable duration exists:
reuse it.

Do NOT hardcode the duration in multiple places.

==================================================
2. HOLD TIMER
==================================================

Display:

🔒 Seat Held

"Reserved for you for 04:59"

Countdown:

05:00
04:59
04:58
...
00:01
00:00

Use javax.swing.Timer or the existing timer mechanism.

The timer must NOT block the Swing EDT.

==================================================
3. HOLD EXPIRATION
==================================================

When timer reaches zero:

Automatically release the seat.

Change:

HELD → AVAILABLE

Update the database.

Refresh the seat map.

Show:

"Your seat hold has expired."

The user must then select the seat again.

==================================================
4. USER-SPECIFIC HOLD
==================================================

A held seat must belong to:

- user ID
- show ID
- seat ID
- hold expiration time

Suggested fields if required:

seat_hold
- id
- show_id
- seat_id
- user_id
- held_at
- expires_at
- status

Possible status:

ACTIVE
EXPIRED
CONVERTED

Reuse existing booking/seat structures if equivalent functionality already exists.

==================================================
5. PREVENT DOUBLE HOLD
==================================================

Two users must NOT be able to hold the same seat.

Example:

User A selects A10.

User B attempts A10.

User B must receive:

"Seat A10 is currently unavailable."

The seat must visually appear:

🔴 Held / Unavailable

==================================================
6. BOOKED SEATS
==================================================

Already booked seats must remain:

BOOKED

They cannot be selected.

State priority:

BOOKED
HELD BY OTHER USER
AVAILABLE
HELD BY CURRENT USER
SELECTED

Do not allow a user to override BOOKED or another user's ACTIVE hold.

==================================================
7. REAL-TIME REFRESH
==================================================

Refresh seat availability periodically while the seat-selection screen is open.

Suggested interval:

5–10 seconds

Do not continuously hammer the database.

Only refresh the current show.

If another user books/holds a seat:

update the UI.

Do not reset the user's valid selected seats unnecessarily.

==================================================
8. MULTIPLE SEATS
==================================================

Support existing multiple-seat selection.

Example:

User selects:

A1
A2
A3

All three seats should be held atomically where possible.

If one seat cannot be held:

do not leave the other seats incorrectly locked.

Show:

"One or more selected seats are no longer available."

Refresh the seat map.

==================================================
9. BOOKING CONVERSION
==================================================

When booking succeeds:

HELD → BOOKED

The temporary hold record should become:

CONVERTED

or be safely removed according to the existing database architecture.

The final booking must contain the actual seats purchased.

==================================================
10. PAYMENT / CHECKOUT FAILURE
==================================================

If CineBook has payment integration:

If payment fails:

release the held seats.

If payment is cancelled:

release the held seats.

If payment succeeds:

convert holds to BOOKED.

If payment is not implemented yet:
make the booking flow compatible with future payment integration.

==================================================
11. DATABASE TRANSACTION
==================================================

Seat hold must be transaction-safe.

Use:

BEGIN TRANSACTION

Check availability

Create hold

COMMIT

If anything fails:

ROLLBACK

Use database constraints where practical.

Do not rely only on Java-side checks.

==================================================
12. AUTOMATIC CLEANUP
==================================================

Expired holds must be cleaned.

Use one centralized cleanup mechanism.

Possible approaches:

- scheduled background cleanup
- cleanup when querying seat availability
- cleanup before creating a new hold

Do NOT create multiple independent schedulers.

The cleanup must not interfere with normal UI operations.

==================================================
13. SEAT MAP UI
==================================================

Maintain existing CineBook seat map.

Use clear visual states:

AVAILABLE
SELECTED
HELD BY ME
HELD BY OTHER USER
BOOKED
BLOCKED

Use existing blue/purple cinematic theme.

Do not use confusing colors if the existing theme has a defined palette.

Add a legend:

● Available
● Selected
● Held by You
● Held by Another User
● Booked
● Blocked

==================================================
14. HOLD SUMMARY
==================================================

Booking summary should show:

Movie
Theatre
Screen
Date
Show time

Selected seats

Hold expiration

Ticket price

Total amount

Example:

Seats:
A10, A11

Hold expires in:
04:32

Total:
₹600

==================================================
15. SECURITY
==================================================

A user must only be able to release their own active holds.

Admin can manage stale holds according to existing RBAC.

Do not trust client-side user IDs.

Use authenticated session/user information.

==================================================
16. CONCURRENCY TEST
==================================================

Simulate:

User A selects Seat A10.

User B selects Seat A10 at nearly the same time.

Expected:

Only one user receives the hold.

The other receives:

"Seat is no longer available."

This is a critical test.

==================================================
17. PERFORMANCE
==================================================

Do not reload the entire application.

Only refresh:

current show
current screen
current seat availability

Use background operations.

Never block Swing EDT with database/network calls.

==================================================
18. ERROR HANDLING
==================================================

Handle:

Database unavailable
Hold creation failure
Hold expiration failure
Concurrent booking
Network failure
Invalid show
Invalid seat
Expired hold
Already booked seat

Never crash the application.

==================================================
19. INTEGRATION
==================================================

Integrate with:

Advanced Seat Selection
Dynamic Pricing
Booking
Cancellation
QR Ticket
Email Notification
In-App Notification
Show Scheduling
User Dashboard
Admin Dashboard

Do not create duplicate booking logic.

==================================================
20. TESTING
==================================================

Test:

1. Hold available seat
2. Hold multiple seats
3. Countdown
4. Hold expiration
5. Releasing expired seat
6. Another user blocked
7. Booking converts hold to booked
8. Payment failure release
9. Cancellation release
10. Concurrent seat selection
11. Database failure
12. Application restart
13. Multiple shows
14. Multiple screens
15. Different users
16. Real-time refresh

Run:

mvn clean compile
mvn test
mvn clean package

==================================================
FINAL REQUIREMENT
==================================================

Do not automatically start Feature 19.

At the end provide:

- files changed
- database changes
- seat-hold architecture
- concurrency handling
- UI changes
- tests performed
- build result
- any configuration required

FEATURE 18 ONLY.
PRESERVE ALL EXISTING CINEBOOK FUNCTIONALITY.


CINEBOOK – FEATURE 19
ADMIN SECURITY + ROLE-BASED ACCESS CONTROL + AUDIT LOG

IMPORTANT:
Do NOT redesign CineBook.
Do NOT remove existing functionality.
Do NOT modify unrelated modules.

First inspect the complete existing authentication, User model, Admin model,
controllers, DAO classes, database schema and existing Admin Dashboard.

Use the existing MVC + DAO architecture.

==================================================
1. ROLE-BASED ACCESS CONTROL
==================================================

Implement secure role-based access control.

Supported roles:

ADMIN
MANAGER
STAFF
USER

Reuse existing roles if already implemented.

Do NOT create duplicate authentication systems.

==================================================
2. PERMISSIONS
==================================================

Create permission-based access where practical.

ADMIN:
- Manage users
- Manage movies
- Manage theatres
- Manage screens
- Manage shows
- Manage seats
- Manage pricing
- Manage bookings
- Manage cancellations
- Manage notifications
- View analytics
- View audit logs
- Manage system settings

MANAGER:
- Manage movies
- Manage theatres
- Manage screens
- Manage shows
- View bookings
- View analytics

STAFF:
- View movies
- View theatres
- View shows
- View bookings
- Handle customer booking operations

USER:
- Browse movies
- Search movies
- Favorites
- Recommendations
- Book tickets
- View tickets
- Cancel eligible bookings

Use the existing authorization architecture if present.

==================================================
3. UI ACCESS CONTROL
==================================================

Hide or disable unauthorized Admin Dashboard modules.

Example:

Staff should NOT see:

System Settings
User Management
Audit Logs

Do not rely only on hiding buttons.

Every sensitive operation must also be validated server/service/database side.

==================================================
4. AUDIT LOG
==================================================

Create an audit log system.

Suggested table:

audit_logs

- id
- user_id
- username
- role
- action
- module
- entity_type
- entity_id
- description
- timestamp
- ip/address if already available

Do not store passwords or sensitive credentials.

==================================================
5. ACTIONS TO LOG
==================================================

Record important actions:

LOGIN_SUCCESS
LOGIN_FAILED
LOGOUT

MOVIE_CREATED
MOVIE_UPDATED
MOVIE_DELETED

THEATRE_CREATED
THEATRE_UPDATED
THEATRE_DELETED

SCREEN_CREATED
SCREEN_UPDATED
SCREEN_DELETED

SHOW_CREATED
SHOW_UPDATED
SHOW_CANCELLED

PRICE_CREATED
PRICE_UPDATED

BOOKING_CREATED
BOOKING_CANCELLED

USER_CREATED
USER_UPDATED
USER_DISABLED

NOTIFICATION_CREATED

SETTINGS_CHANGED

Use existing event/action names if equivalent functionality exists.

==================================================
6. AUDIT LOG SCREEN
==================================================

Add:

"🔐 Audit Logs"

to the Admin Dashboard.

Display:

Date/Time
User
Role
Action
Module
Description

Provide:

Search
Date filter
User filter
Role filter
Module filter
Action filter

Add:

Clear Filters

Do NOT load unlimited historical logs at once.

Use pagination where practical.

==================================================
7. SECURITY
==================================================

Never store:

Passwords
API keys
SMTP passwords
Database passwords

inside audit logs.

Mask sensitive information.

Example:

API_KEY=********

==================================================
8. FAILED LOGIN TRACKING
==================================================

Track failed login attempts.

Example:

"Failed login attempt"

Do not expose whether a username/email exists.

If an existing account lockout system exists:
reuse it.

Otherwise implement a safe configurable threshold.

Default:

5 failed attempts

Do not permanently lock users without an admin/recovery mechanism.

==================================================
9. SESSION SECURITY
==================================================

Ensure logout clears the active authenticated session.

Prevent unauthorized access to Admin screens.

If the user session expires:
redirect to Login.

Do not expose protected data after logout.

==================================================
10. DATABASE SECURITY
==================================================

Use PreparedStatements.

Use transactions for critical operations.

Do not expose SQL errors directly to users.

==================================================
11. PERFORMANCE
==================================================

Audit logging must not make the UI slow.

Use background processing where appropriate.

Do not block the Swing EDT unnecessarily.

==================================================
12. TESTING
==================================================

Test:

1. Admin access
2. Manager access
3. Staff access
4. User access
5. Unauthorized operation
6. Successful login log
7. Failed login log
8. Logout log
9. Movie modification log
10. Show modification log
11. Booking log
12. Audit filtering
13. Session logout
14. Database failure
15. Password/API key protection

Run:

mvn clean compile
mvn test
mvn clean package

==================================================
FINAL REQUIREMENT
==================================================

Preserve all existing CineBook functionality.

Do not automatically start Feature 20.

At the end report:

- files changed
- database changes
- roles
- permissions
- audit events
- security improvements
- tests
- build result

FEATURE 19 ONLY.