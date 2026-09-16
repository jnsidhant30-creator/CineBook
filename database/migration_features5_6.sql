-- Migration for Feature 5: Multi-Theatre, Screens, and Dynamic Capacity

-- 1. Add new columns to theatres (with defaults to avoid breaking existing data)
ALTER TABLE theatres ADD COLUMN city VARCHAR(100) DEFAULT 'Unknown City';
ALTER TABLE theatres ADD COLUMN contact_number VARCHAR(20);
ALTER TABLE theatres ADD COLUMN num_screens INT DEFAULT 1;
ALTER TABLE theatres ADD COLUMN status VARCHAR(20) DEFAULT 'ACTIVE';

-- 2. Create screens table
CREATE TABLE IF NOT EXISTS screens (
    screen_id INT PRIMARY KEY AUTO_INCREMENT,
    theatre_id INT NOT NULL,
    screen_name VARCHAR(50) NOT NULL,
    screen_type VARCHAR(20) DEFAULT 'STANDARD',
    capacity INT NOT NULL DEFAULT 100,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    FOREIGN KEY (theatre_id) REFERENCES theatres(theatre_id)
);

-- 3. Add screen_id to shows and seats (temporarily nullable)
ALTER TABLE shows ADD COLUMN screen_id INT;
ALTER TABLE seats ADD COLUMN screen_id INT;

-- 4. Create a default screen for every existing theatre
INSERT INTO screens (theatre_id, screen_name, screen_type, capacity, status)
SELECT theatre_id, 'Screen 1', 'STANDARD', total_seats, 'ACTIVE' FROM theatres;

-- 5. Update existing shows and seats to point to the newly created default screen
UPDATE shows s
JOIN screens sc ON s.theatre_id = sc.theatre_id
SET s.screen_id = sc.screen_id;

UPDATE seats s
JOIN screens sc ON s.theatre_id = sc.theatre_id
SET s.screen_id = sc.screen_id;

-- 6. Now enforce NOT NULL constraints
ALTER TABLE shows MODIFY COLUMN screen_id INT NOT NULL;
ALTER TABLE seats MODIFY COLUMN screen_id INT NOT NULL;

-- 7. Add foreign keys for screen_id
ALTER TABLE shows ADD CONSTRAINT fk_shows_screen FOREIGN KEY (screen_id) REFERENCES screens(screen_id);
ALTER TABLE seats ADD CONSTRAINT fk_seats_screen FOREIGN KEY (screen_id) REFERENCES screens(screen_id);

-- 8. Drop the old unique constraint on seats and create a new one based on screen_id
ALTER TABLE seats DROP INDEX theatre_id;
CREATE UNIQUE INDEX uk_screen_seat ON seats (screen_id, seat_number);
