USE movie_ticket_management;

INSERT INTO coupons (code, description, discount_type, discount_value, max_discount, min_booking_amount, usage_limit, per_user_limit, start_date, expiry_date, active, allow_cinepoints) VALUES
('WELCOME10', '10% off for new users', 'PERCENTAGE', 10.00, 100.00, 200.00, 100, 1, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), true, true),
('CINE50', 'Flat ₹50 off on all bookings', 'FIXED', 50.00, NULL, 300.00, 500, NULL, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), true, true),
('NOCINEPOINTS100', 'Flat ₹100 off, cannot combine with CinePoints', 'FIXED', 100.00, NULL, 500.00, 100, 2, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), true, false);
