-- ============================================
-- CREATE DATABASE
-- ============================================
CREATE DATABASE IF NOT EXISTS movie_ticket_management;
USE movie_ticket_management;

-- ============================================
-- CREATE USERS TABLE
-- ============================================
CREATE TABLE users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL
);

-- ============================================
-- CREATE MOVIES TABLE
-- ============================================
CREATE TABLE movies (
    movie_id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(100) NOT NULL,
    genre VARCHAR(50),
    duration INT NOT NULL,
    language VARCHAR(50),
    rating DECIMAL(2,1),
    release_date DATE NULL
);

-- ============================================
-- CREATE THEATRES TABLE
-- ============================================
CREATE TABLE theatres (
    theatre_id INT PRIMARY KEY AUTO_INCREMENT,
    theatre_name VARCHAR(100) NOT NULL,
    location VARCHAR(150),
    total_seats INT NOT NULL
);

-- ============================================
-- CREATE SHOWS TABLE
-- ============================================
CREATE TABLE shows (
    show_id INT PRIMARY KEY AUTO_INCREMENT,
    movie_id INT NOT NULL,
    theatre_id INT NOT NULL,
    show_date DATE NOT NULL,
    show_time TIME NOT NULL,
    ticket_price DECIMAL(10,2) NOT NULL,

    FOREIGN KEY (movie_id)
        REFERENCES movies(movie_id),

    FOREIGN KEY (theatre_id)
        REFERENCES theatres(theatre_id)
);

-- ============================================
-- CREATE SEATS TABLE
-- ============================================
CREATE TABLE seats (
    seat_id INT PRIMARY KEY AUTO_INCREMENT,
    theatre_id INT NOT NULL,
    seat_number VARCHAR(10) NOT NULL,
    status VARCHAR(20) DEFAULT 'AVAILABLE',

    FOREIGN KEY (theatre_id)
        REFERENCES theatres(theatre_id),

    UNIQUE (theatre_id, seat_number)
);

-- ============================================
-- CREATE BOOKINGS TABLE
-- ============================================
CREATE TABLE bookings (
    booking_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    show_id INT NOT NULL,
    booking_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) DEFAULT 'CONFIRMED',
    is_verified BOOLEAN DEFAULT FALSE,
    verified_at DATETIME NULL,

    FOREIGN KEY (user_id)
        REFERENCES users(user_id),

    FOREIGN KEY (show_id)
        REFERENCES shows(show_id)
);

-- ============================================
-- CREATE BOOKING_DETAILS TABLE
-- ============================================
CREATE TABLE booking_details (
    id INT PRIMARY KEY AUTO_INCREMENT,
    booking_id INT NOT NULL,
    seat_id INT NOT NULL,
    price DECIMAL(10,2) NOT NULL,

    FOREIGN KEY (booking_id)
        REFERENCES bookings(booking_id),

    FOREIGN KEY (seat_id)
        REFERENCES seats(seat_id)
);

-- ============================================
-- CREATE NOTIFICATIONS TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS notifications (
    notification_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    title VARCHAR(120) NOT NULL,
    message TEXT NOT NULL,
    notification_type VARCHAR(50) DEFAULT 'GENERAL',
    is_read BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id)
        REFERENCES users(user_id) ON DELETE CASCADE
);

-- ============================================
-- CREATE WATCHLIST TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS watchlist (
    watchlist_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    movie_id INT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (movie_id) REFERENCES movies(movie_id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_movie_watchlist (user_id, movie_id)
);

-- ============================================
-- CREATE RECENTLY VIEWED TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS recently_viewed (
    viewed_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    movie_id INT NOT NULL,
    viewed_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (movie_id) REFERENCES movies(movie_id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_movie_viewed (user_id, movie_id)
);

-- ============================================
-- CREATE FAVORITES TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS favorites (
    favorite_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    movie_id INT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (movie_id) REFERENCES movies(movie_id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_movie_favorite (user_id, movie_id)
);

-- ============================================
-- CREATE PAYMENTS TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS payments (
    payment_id INT PRIMARY KEY AUTO_INCREMENT,
    booking_id INT NOT NULL,
    transaction_id VARCHAR(50) UNIQUE NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    payment_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    payment_date DATETIME DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (booking_id) REFERENCES bookings(booking_id) ON DELETE CASCADE
);

-- ============================================
-- CREATE INDEXES
-- ============================================
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_movies_title ON movies(title);
CREATE INDEX idx_shows_date ON shows(show_date);
CREATE INDEX idx_shows_movie_id ON shows(movie_id);
CREATE INDEX idx_shows_theatre_id ON shows(theatre_id);
CREATE INDEX idx_bookings_user_id ON bookings(user_id);
CREATE INDEX idx_bookings_show_id ON bookings(show_id);
CREATE INDEX idx_notifications_user ON notifications(user_id, is_read);
CREATE INDEX idx_watchlist_user ON watchlist(user_id);
CREATE INDEX idx_recently_viewed_user ON recently_viewed(user_id, viewed_at);
CREATE INDEX idx_favorites_user ON favorites(user_id, created_at);

-- ============================================
-- CREATE UPCOMING_MOVIES TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS upcoming_movies (
    id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(150) NOT NULL,
    release_date DATE NULL,
    imdb_id VARCHAR(20) UNIQUE,
    poster_url VARCHAR(255),
    genre VARCHAR(100),
    description TEXT,
    status VARCHAR(20) DEFAULT 'NEW',
    discovered_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    last_checked DATETIME DEFAULT CURRENT_TIMESTAMP,
    source VARCHAR(50) DEFAULT 'SIMULATED'
);

CREATE INDEX idx_upcoming_movies_status ON upcoming_movies(status);
CREATE INDEX idx_upcoming_movies_release ON upcoming_movies(release_date);
