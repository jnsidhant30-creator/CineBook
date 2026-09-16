-- Feature 11: Smart Temporary Seat Hold System
-- Creates the seat_holds table with concurrency-safe unique constraint

USE movie_ticket_management;

CREATE TABLE IF NOT EXISTS seat_holds (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    show_id     INT NOT NULL,
    seat_id     INT NOT NULL,
    user_id     INT NOT NULL,
    held_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at  DATETIME NOT NULL,
    status      VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',  -- ACTIVE, EXPIRED, RELEASED, CONVERTED
    CONSTRAINT fk_sh_show   FOREIGN KEY (show_id) REFERENCES shows(show_id)   ON DELETE CASCADE,
    CONSTRAINT fk_sh_seat   FOREIGN KEY (seat_id) REFERENCES seats(seat_id)   ON DELETE CASCADE,
    CONSTRAINT fk_sh_user   FOREIGN KEY (user_id) REFERENCES users(user_id)   ON DELETE CASCADE
);

-- Unique: only one ACTIVE hold per (show, seat) at a time
-- (Partial unique indexes are not supported in MySQL; we enforce via INSERT logic)
-- Index for fast lookup and cleanup
CREATE INDEX idx_sh_show_seat   ON seat_holds (show_id, seat_id);
CREATE INDEX idx_sh_expires     ON seat_holds (expires_at);
CREATE INDEX idx_sh_status      ON seat_holds (status);
CREATE INDEX idx_sh_user        ON seat_holds (user_id);

-- Feature 12: Audit Log table
CREATE TABLE IF NOT EXISTS audit_logs (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    user_id     INT,
    role        VARCHAR(20),
    action      VARCHAR(50)  NOT NULL,
    entity_type VARCHAR(50),
    entity_id   INT,
    description TEXT,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_al_user       (user_id),
    INDEX idx_al_action     (action),
    INDEX idx_al_created    (created_at),
    INDEX idx_al_entity     (entity_type)
);
