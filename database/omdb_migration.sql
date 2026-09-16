-- ============================================================
-- OMDb API Integration Migration
-- Adds imdb_id and poster_url columns to the movies table.
-- Safe to run multiple times (uses IF NOT EXISTS pattern).
-- Does NOT drop, truncate, or modify any existing data.
-- ============================================================

USE movie_ticket_management;

-- Add imdb_id column (used as external identifier for duplicate prevention)
ALTER TABLE movies
    ADD COLUMN IF NOT EXISTS imdb_id VARCHAR(20) NULL DEFAULT NULL COMMENT 'IMDb identifier (e.g. tt0499549) — populated by OMDb import';

-- Add poster_url column (stores the OMDb poster image URL)
ALTER TABLE movies
    ADD COLUMN IF NOT EXISTS poster_url VARCHAR(500) NULL DEFAULT NULL COMMENT 'OMDb poster image URL';

-- Add a unique index on imdb_id (only where not null, to allow multiple null entries)
-- MySQL unique index allows multiple NULLs, so existing movies without imdb_id are unaffected.
CREATE UNIQUE INDEX IF NOT EXISTS idx_movies_imdb_id
    ON movies (imdb_id);

-- Verify
SELECT 'Migration complete. Columns added: imdb_id, poster_url' AS status;
DESCRIBE movies;
