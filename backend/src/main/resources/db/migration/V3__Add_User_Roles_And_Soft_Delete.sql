-- ==========================================
-- PHASE 7: ENTERPRISE IAM & SOFT DELETE
-- ==========================================

-- 1. Add Role-Based Access Control (RBAC) column to users
-- Defaults existing and newly registered standard users to 'USER'
ALTER TABLE users ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'USER';

-- 2. Add Soft Delete flag to users
-- Defaults all existing accounts to active (TRUE)
ALTER TABLE users ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE;