# Project Recovery and Implementation Plan (Phases 1-6)

This plan outlines the steps to complete Phases 1-6 of the Movie Ticket Management System, as requested in the Master Prompt.

## Proposed Changes

### 1. Model & Schema Refactoring
- **[MODIFY]** `database/schema.sql` & `database/sample_data.sql`: 
  - Change `booking_seats` table to `booking_details` with fields `id`, `booking_id`, `seat_id`, `price` to match Phase 2 requirements (Section 9.7).
- **[RENAME/MODIFY]** `src/main/java/com/movieticket/model/BookingSeat.java` -> `BookingDetail.java`:
  - Update fields to `id`, `bookingId`, `seatId`, `price`.
- **[RENAME/MODIFY]** `src/main/java/com/movieticket/dao/BookingSeatDAO.java` -> `BookingDetailDAO.java`:
  - Implement required operations: `addBookingDetail()`, `getBookingDetails()`.

### 2. DAO Layer Enhancements (Phase 5)
- **[MODIFY]** `src/main/java/com/movieticket/dao/UserDAO.java`: Add `countUsers()`. Fix exception swallowing (throw `SQLException` or `RuntimeException` so the controller knows the DB connection failed instead of returning empty user).
- **[MODIFY]** `src/main/java/com/movieticket/dao/MovieDAO.java`: Add `countMovies()`.
- **[MODIFY]** `src/main/java/com/movieticket/dao/TheatreDAO.java`: Add `countTheatres()`.
- **[MODIFY]** `src/main/java/com/movieticket/dao/ShowDAO.java`: Add `countShows()`.
- **[MODIFY]** `src/main/java/com/movieticket/dao/BookingDAO.java`: Add `countBookings()`.

### 3. Authentication & Error Handling (Phase 6)
- **[MODIFY]** `src/main/java/com/movieticket/controller/AuthenticationController.java`:
  - Update login logic to correctly handle database connection errors versus invalid credentials.
- **[MODIFY]** `src/main/java/com/movieticket/view/AdminDashboardFrame.java` & `AdminDashboardController` (if applicable):
  - Add statistics display for Total Movies, Total Theatres, Total Shows, Total Users, and Total Bookings.
  - Fetch real counts using the new `count*()` DAO methods.

### 4. Database Setup & Connection
- **[IMPORTANT]** The database connection is currently failing because the MySQL root password in `db.properties` (`root123`) does not match the actual MySQL server password. 
- I will provide clear instructions on how to set up the database using the correct password so you can finally run the app successfully.

## Open Questions
- Do you happen to know your actual MySQL root password? (If yes, you won't need to run the reset script, we can just update `db.properties`).

## Verification Plan
- Run `mvn clean compile` to ensure all code compiles perfectly.
- Provide instructions to run `schema.sql` and `sample_data.sql` with your actual MySQL password.
- Run the application via `mvn exec:java -Dexec.mainClass=com.movieticket.Main` to verify the login flow.
