-- TMDB Migration Script
-- Adds TMDB specific columns to the existing movies table.
-- Existing booking and OMDb data is preserved.

ALTER TABLE movies
    ADD COLUMN tmdb_id         INT          NULL,
    ADD COLUMN tmdb_poster_path VARCHAR(200) NULL,
    ADD COLUMN tmdb_backdrop_path VARCHAR(200) NULL,
    ADD COLUMN tmdb_vote_avg   DECIMAL(4,2) NULL,
    ADD COLUMN tmdb_vote_count INT          NULL,
    ADD COLUMN tmdb_popularity DECIMAL(10,4) NULL,
    ADD COLUMN tmdb_last_updated DATETIME     NULL;

CREATE UNIQUE INDEX idx_movies_tmdb_id ON movies (tmdb_id);
