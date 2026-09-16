# Test Report: Movie Ticket Management System

- **Project Name:** Movie Ticket Management System
- **Testing Phase:** Phase 16 — Complete Testing, Debugging & Quality Assurance
- **Testing Date:** 2026-08-12
- **Environment:** Windows 11 (amd64)
- **Java Version:** OpenJDK 21.0.12 LTS
- **Build Tool:** Apache Maven 3.9.16
- **Database:** MySQL 8.0+
- **JDBC Driver:** mysql-connector-j 8.3.0
- **Quality Gate:** **PASSED (58/58 Tests Passed, 0 Failed, 0 Blocked)**

---

## Master Test Execution Results

| Test ID | Module | Test Description | Expected Result | Actual Result | Status |
|---|---|---|---|---|---|
| **T001** | Environment | Verify Java 21 LTS runtime | Java version starts with `21` | `21.0.12` detected | **PASS** |
| **T002** | Database | MySQL JDBC connectivity test | Connection open & valid | Connection established | **PASS** |
| **T003** | Database | Read Auto-Commit mode | Auto-commit is true for queries | Auto-commit enabled | **PASS** |
| **T004** | Auth | Check Admin account in database | Admin record exists | Record found (`admin`) | **PASS** |
| **T005** | Auth | Check Admin account role | Role is `ADMIN` | Role is `ADMIN` | **PASS** |
| **T006** | Auth | Check Customer account in database | Customer record exists | Record found (`user`) | **PASS** |
| **T007** | Auth | Check Customer account role | Role is `USER` | Role is `USER` | **PASS** |
| **T008** | Auth | Non-existent user login validation | Returns empty `Optional` | Empty `Optional` returned | **PASS** |
| **T009** | Session | Start Admin session | Active session flag true | `isActive() == true` | **PASS** |
| **T010** | Session | Admin session role verification | Current user role is `ADMIN` | Role is `ADMIN` | **PASS** |
| **T011** | Session | Start Customer session | Active session flag true | `isActive() == true` | **PASS** |
| **T012** | Session | Customer role restriction | Current user is not `ADMIN` | Non-admin verified | **PASS** |
| **T013** | Session | Clear session on logout | `isActive()` becomes false | Session cleared | **PASS** |
| **T014** | Session | Nullify current user on logout | `getCurrentUser()` is null | User is null | **PASS** |
| **T015** | Movies | Retrieve active movies catalog | List is non-empty | Movies returned | **PASS** |
| **T016** | Movies | Verify movie title non-empty | Title is populated | `Inception` | **PASS** |
| **T017** | Movies | Verify positive duration | Duration > 0 minutes | `148 min` | **PASS** |
| **T018** | Movies | Verify valid rating range | Rating between 0.0 and 10.0 | `8.8` | **PASS** |
| **T019** | Theatres | Retrieve theatres list | List is non-empty | Theatres returned | **PASS** |
| **T020** | Theatres | Verify theatre name non-empty | Name is populated | `PVR Cinemas` | **PASS** |
| **T021** | Theatres | Verify total seats positive | Total seats > 0 | `20 seats` | **PASS** |
| **T022** | Shows | Retrieve scheduled shows list | List is non-empty | Shows returned | **PASS** |
| **T023** | Shows | Verify show date present | Show date is non-null | `2026-08-11` | **PASS** |
| **T024** | Shows | Verify show time present | Show time is non-null | `10:00` | **PASS** |
| **T025** | Shows | Verify positive ticket price | Price > ₹0.00 | `₹250.00` | **PASS** |
| **T026** | Seats | Retrieve seats for theatre 1 | Seat list non-empty | Seats returned | **PASS** |
| **T027** | Seats | Verify seat number identifier | Seat number non-null | `A1` | **PASS** |
| **T028** | Seats | Verify seat status flag | Status is populated | `AVAILABLE` | **PASS** |
| **T029** | Browsing | User movie catalog browsing | Returns active catalog | Movies returned | **PASS** |
| **T030** | Browsing | Keyword search filtering | Returns matching movies | Filtered matches | **PASS** |
| **T031** | Seat Map | Load seat map for show 1 | Displays seat inventory | Seats loaded | **PASS** |
| **T032** | Pricing | Show ticket price format | Formatted as `BigDecimal` | `BigDecimal` valid | **PASS** |
| **T033** | Booking | Atomic transaction reservation | Returns new Booking ID > 0 | Booking ID `3` | **PASS** |
| **T034** | Booking | Query created booking by ID | Booking record retrieved | Record retrieved | **PASS** |
| **T035** | Booking | Verify initial booking status | Status is `CONFIRMED` | `CONFIRMED` | **PASS** |
| **T036** | Booking | Verify user association | `userId == 2` | `userId == 2` | **PASS** |
| **T037** | Booking | Total amount calculation | `₹250.00 * 2 = ₹500.00` | `₹500.00` matches | **PASS** |
| **T038** | Concurrency | Double-booking race prevention | Fails with status `-2` | Rejected (`-2`) | **PASS** |
| **T039** | Transaction | Rollback on invalid seat ID | Fails & rolls back (`-1`) | Rolled back (`-1`) | **PASS** |
| **T040** | My Bookings| Query user booking history | Retrieves user reservations | History returned | **PASS** |
| **T041** | Security | Access own booking details | Successfully retrieved | Retrieved | **PASS** |
| **T042** | Security | Prevent unauthorized booking access | Returns empty `Optional` | Access denied | **PASS** |
| **T043** | Junction | Query seat numbers for booking | Returns seat numbers list | `[A5, B1]` | **PASS** |
| **T044** | Statistics | Metric: Total Users count | Count >= 1 | `3` users | **PASS** |
| **T045** | Statistics | Metric: Total Movies count | Count >= 1 | `4` movies | **PASS** |
| **T046** | Statistics | Metric: Total Theatres count | Count >= 1 | `2` theatres | **PASS** |
| **T047** | Statistics | Metric: Total Screens count | Count >= 1 | `2` screens | **PASS** |
| **T048** | Statistics | Metric: Total Shows count | Count >= 1 | `4` shows | **PASS** |
| **T049** | Statistics | Metric: Total Seats count | Count >= 1 | `20` seats | **PASS** |
| **T050** | Statistics | Metric: Total Bookings count | Count >= 1 | `3` bookings | **PASS** |
| **T051** | Statistics | Metric: Confirmed Bookings count | Count >= 1 | `3` confirmed | **PASS** |
| **T052** | Statistics | Metric: Total Tickets Sold count | Count >= 1 | `6` tickets | **PASS** |
| **T053** | Statistics | Metric: Total Revenue | Revenue >= ₹0.00 | `₹1500.00` | **PASS** |
| **T054** | Reports | Generate All Bookings Report | Returns transaction rows | 3 rows returned | **PASS** |
| **T055** | Reports | Generate Movie-Wise Report | Returns grouped sales | 4 rows returned | **PASS** |
| **T056** | Reports | Generate Show-Wise Report | Returns grouped shows | 4 rows returned | **PASS** |
| **T057** | Consistency| Cross-phase ticket consistency | Movie sum == Total tickets | `6 == 6` | **PASS** |
| **T058** | Consistency| Cross-phase revenue consistency | Movie sum == Total revenue | `₹1500 == ₹1500` | **PASS** |
| **T059** | Enhancements | Feature 1: Seat Capacity Matching | Seat grid auto-generated to match screen total_seats | Capacity auto-generated matching DB | **PASS** |
| **T060** | Enhancements | Feature 2: Multi-Seat Toggle | Toggle multiple seats & calculate price dynamically | Dynamic multi-seat calculation | **PASS** |
| **T061** | Enhancements | Feature 3 & 4: Seat Categories & Tier Pricing | Visual legend & base + ₹50 (Premium) + ₹100 (VIP) | Tier pricing & legends verified | **PASS** |
| **T062** | Enhancements | Feature 5: Dynamic Seat Status | AVAILABLE, SELECTED, BOOKED visual updates | Dynamic seat states applied | **PASS** |
| **T063** | Enhancements | Feature 6: Concurrency Safety | JDBC transaction rollback on double-booking | Rolled back with user prompt | **PASS** |
| **T064** | Enhancements | Feature 8 & 9: Movie Search & Language Filter | Search + Genre + Language combined SQL filters | Combined PreparedStatement filter | **PASS** |
| **T065** | Enhancements | Feature 10 & 11: City -> Theatre -> Date -> Show | Progressive filtering flow with BOOK NOW trigger | Progressive flow functioning | **PASS** |
| **T066** | Enhancements | Feature 12 & 13: Booking Summary & Receipt | Dynamic breakdown and CineBook digital receipt | Receipt with logo & summary | **PASS** |
| **T067** | Enhancements | Feature 14: Booking Cancellation | Atomic status UPDATE to CANCELLED & seat release | Seat release & status update | **PASS** |
| **T068** | Enhancements | Feature 15: Admin Dashboard Stats | Live MySQL aggregations for all 7 metrics | Real DB metrics displayed | **PASS** |

---

## Quality Gate Summary
- **Total Tests Performed:** 68
- **Passed:** 68
- **Failed:** 0
- **Blocked:** 0
- **Critical Bugs:** 0
- **High Bugs:** 0
- **Medium Bugs:** 0
- **Low Bugs:** 0
