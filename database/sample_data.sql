-- ============================================
-- INSERT SAMPLE DATA
-- ============================================
USE movie_ticket_management;

-- ============================================
-- USERS
-- ============================================
-- DEMO CREDENTIALS
INSERT INTO users (username, password, role) VALUES 
('admin', 'admin123', 'ADMIN'),
('user', 'user123', 'USER');

-- ============================================
-- MOVIES
-- ============================================
INSERT INTO movies (title, genre, duration, language, rating) VALUES 
('Inception', 'Sci-Fi', 148, 'English', 8.8),
('Interstellar', 'Sci-Fi', 169, 'English', 8.6),
('Dangal', 'Biography', 161, 'Hindi', 8.4),
('Spider-Man: No Way Home', 'Action', 148, 'English', 8.2);

-- ============================================
-- THEATRES
-- ============================================
INSERT INTO theatres (theatre_name, location, total_seats) VALUES 
('PVR Cinemas', 'City Mall', 20),
('INOX', 'Downtown', 20);

-- ============================================
-- SEATS
-- ============================================
-- Insert some sample seats for Theatre 1 (PVR)
INSERT INTO seats (theatre_id, seat_number, status) VALUES 
(1, 'A1', 'AVAILABLE'),
(1, 'A2', 'AVAILABLE'),
(1, 'A3', 'AVAILABLE'),
(1, 'A4', 'AVAILABLE'),
(1, 'A5', 'AVAILABLE'),
(1, 'B1', 'AVAILABLE'),
(1, 'B2', 'AVAILABLE'),
(1, 'B3', 'AVAILABLE'),
(1, 'B4', 'AVAILABLE'),
(1, 'B5', 'AVAILABLE');

-- Insert some sample seats for Theatre 2 (INOX)
INSERT INTO seats (theatre_id, seat_number, status) VALUES 
(2, 'A1', 'AVAILABLE'),
(2, 'A2', 'AVAILABLE'),
(2, 'A3', 'AVAILABLE'),
(2, 'A4', 'AVAILABLE'),
(2, 'A5', 'AVAILABLE'),
(2, 'B1', 'AVAILABLE'),
(2, 'B2', 'AVAILABLE'),
(2, 'B3', 'AVAILABLE'),
(2, 'B4', 'AVAILABLE'),
(2, 'B5', 'AVAILABLE');

-- ============================================
-- SHOWS
-- ============================================
INSERT INTO shows (movie_id, theatre_id, show_date, show_time, ticket_price) VALUES 
(1, 1, '2026-08-11', '10:00:00', 250.00),
(2, 1, '2026-08-11', '14:00:00', 300.00),
(3, 2, '2026-08-11', '13:00:00', 200.00),
(4, 2, '2026-08-11', '18:00:00', 350.00);

-- ============================================
-- BOOKINGS
-- ============================================
-- Sample booking for User (user_id = 2) for Show 1 (movie_id = 1, PVR)
INSERT INTO bookings (user_id, show_id, total_amount, status) VALUES 
(2, 1, 500.00, 'CONFIRMED');

-- ============================================
-- BOOKING DETAILS
-- ============================================
-- Booking details for the above booking
INSERT INTO booking_details (booking_id, seat_id, price) VALUES 
(1, 1, 250.00), -- Seat A1 for Theatre 1
(1, 2, 250.00); -- Seat A2 for Theatre 1
