-- Migration script for Coupon and Promo Code System

CREATE TABLE IF NOT EXISTS coupons (
    id INT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255),
    discount_type ENUM('PERCENTAGE', 'FIXED') NOT NULL,
    discount_value DECIMAL(10, 2) NOT NULL,
    max_discount DECIMAL(10, 2) DEFAULT NULL,
    min_booking_amount DECIMAL(10, 2) DEFAULT 0.00,
    usage_limit INT DEFAULT NULL,
    per_user_limit INT DEFAULT NULL,
    used_count INT DEFAULT 0,
    start_date DATETIME NOT NULL,
    expiry_date DATETIME NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    allow_cinepoints BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS coupon_usage (
    id INT AUTO_INCREMENT PRIMARY KEY,
    coupon_id INT NOT NULL,
    user_id INT NOT NULL,
    booking_id INT NOT NULL,
    discount_amount DECIMAL(10, 2) NOT NULL,
    used_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (coupon_id) REFERENCES coupons(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (booking_id) REFERENCES bookings(booking_id) ON DELETE CASCADE
);

-- Indexes for performance
CREATE INDEX idx_coupon_code ON coupons(code);
CREATE INDEX idx_coupon_active_dates ON coupons(active, start_date, expiry_date);
CREATE INDEX idx_coupon_usage_lookup ON coupon_usage(coupon_id, user_id);
