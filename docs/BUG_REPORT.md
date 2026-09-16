# Bug Report: Movie Ticket Management System

- **Project:** Movie Ticket Management System
- **Phase:** Phase 16 — Testing, Debugging & Quality Assurance
- **Date:** 2026-08-12
- **Summary:** No critical or high-severity bugs were found during the performed testing. All resolved defects discovered during module development have been systematically tracked, fixed, and verified below.

---

### Discovered & Resolved Defects

#### Bug ID: BUG-001
- **Title:** Duplicate Revenue Sum in Movie-Wise Report Query
- **Module:** `ReportDAO.java`
- **Severity:** MEDIUM
- **Steps to Reproduce:**
  1. Create a booking with multiple seats (e.g. 2 seats).
  2. Join `booking_details` with `bookings` and run `SUM(b.total_amount)`.
- **Expected Result:** Total revenue should equal the single booking amount.
- **Actual Result:** Joining `booking_details` duplicated `b.total_amount` for each reserved seat row.
- **Root Cause:** Cartesian multiplication when joining `booking_details` in group query.
- **Fix:** Used a correlated subquery `COALESCE((SELECT SUM(b2.total_amount) FROM bookings b2 ...), 0.00)` to isolate booking amounts.
- **Status:** **RESOLVED & VERIFIED**

---

#### Bug ID: BUG-002
- **Title:** Schema Column Reference in Report Aggregations
- **Module:** `ReportDAO.java`
- **Severity:** LOW
- **Steps to Reproduce:**
  1. Query `screen_number` on `seats` / `shows` tables where column is omitted from schema.
- **Expected Result:** Query should execute cleanly against MySQL schema.
- **Actual Result:** `SQLException: Unknown column 'screen_number' in 'field list'`.
- **Root Cause:** Schema defines screens implicitly at the theatre level.
- **Fix:** Standardized query to count theatres as screen venues and default display string to `"Screen 1"`.
- **Status:** **RESOLVED & VERIFIED**

---

#### Bug ID: BUG-003
- **Title:** Concurrency Race Condition on Simultaneous Seat Selection
- **Module:** `BookingDAO.java`
- **Severity:** CRITICAL
- **Steps to Reproduce:**
  1. User A and User B select the same seat `A1` for show 1 simultaneously.
  2. Both submit confirmation.
- **Expected Result:** First transaction succeeds; second transaction fails gracefully with warning message.
- **Actual Result:** Without re-check inside transaction, duplicate booking records could be inserted.
- **Root Cause:** Lack of pre-insert validation within atomic transaction lock.
- **Fix:** Added `getBookedSeatIdsForShow` check inside the `conn.setAutoCommit(false)` transaction block before insertion, returning `-2` if any seat was booked.
- **Status:** **RESOLVED & VERIFIED**

---

### Summary of Bug Status
- **Critical Bugs:** 0 Open (1 Resolved)
- **High Bugs:** 0 Open (0 Resolved)
- **Medium Bugs:** 0 Open (1 Resolved)
- **Low Bugs:** 0 Open (1 Resolved)
- **Current Quality Status:** 100% Stable. No open defects.
