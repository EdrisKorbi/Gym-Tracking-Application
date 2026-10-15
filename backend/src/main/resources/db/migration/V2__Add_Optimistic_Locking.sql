-- Add Optimistic Locking version column to all core tables
-- DEFAULT 0 ensures existing rows in the database are safely initialized

ALTER TABLE users ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

ALTER TABLE programs ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE program_phases ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE routines ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE routine_exercises ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

ALTER TABLE workouts ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE exercises ADD COLUMN version BIGINT NOT NULL DEFAULT 0;