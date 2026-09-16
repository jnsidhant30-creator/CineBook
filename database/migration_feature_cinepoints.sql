-- ============================================
-- CINEPOINTS REWARDS SYSTEM SCHEMA
-- ============================================

USE movie_ticket_management;

CREATE TABLE IF NOT EXISTS cinepoints_wallet (
    user_id INT PRIMARY KEY,
    points_balance INT NOT NULL DEFAULT 0,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS cinepoints_transactions (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    booking_id INT NOT NULL,
    points INT NOT NULL,
    transaction_type VARCHAR(20) NOT NULL, -- EARNED, REDEEMED
    description VARCHAR(255),
    balance_after INT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (booking_id) REFERENCES bookings(booking_id) ON DELETE CASCADE,
    UNIQUE KEY uk_booking_transaction (booking_id, transaction_type)
);

CREATE INDEX idx_cinepoints_user_time ON cinepoints_transactions(user_id, created_at);
