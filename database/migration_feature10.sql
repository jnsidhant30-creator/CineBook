-- ============================================
-- FEATURE 10: RECOMMENDATIONS & PREFERENCES
-- ============================================
USE movie_ticket_management;

CREATE TABLE IF NOT EXISTS user_preferences (
    user_id INT PRIMARY KEY,
    preferred_genres VARCHAR(255),
    preferred_languages VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);
