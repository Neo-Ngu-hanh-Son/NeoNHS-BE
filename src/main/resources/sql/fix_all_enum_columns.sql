-- ====================================================================
-- COMPREHENSIVE FIX: All Enum Column Length Issues
-- Database: neonhs_db
-- Date: 2026-02-21
-- ====================================================================
-- This script fixes all enum columns that may have insufficient length
-- ====================================================================

USE neonhs_db;

-- ====================================================================
-- 1. WORKSHOP_TEMPLATES Table
-- ====================================================================
-- Check current status column
SELECT
    COLUMN_NAME,
    COLUMN_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'neonhs_db'
  AND TABLE_NAME = 'workshop_templates'
  AND COLUMN_NAME = 'status';

-- Fix: Increase status column length
ALTER TABLE workshop_templates
MODIFY COLUMN status VARCHAR(20) NOT NULL;

-- ====================================================================
-- 2. WORKSHOP_SESSIONS Table
-- ====================================================================
-- Check current status column
SELECT
    COLUMN_NAME,
    COLUMN_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'neonhs_db'
  AND TABLE_NAME = 'workshop_sessions'
  AND COLUMN_NAME = 'status';

-- Fix: Increase status column length (if table exists)
ALTER TABLE workshop_sessions
MODIFY COLUMN status VARCHAR(20) NOT NULL;

-- ====================================================================
-- 3. USERS Table
-- ====================================================================
-- Check current role column
SELECT
    COLUMN_NAME,
    COLUMN_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'neonhs_db'
  AND TABLE_NAME = 'users'
  AND COLUMN_NAME = 'role';

-- Fix: Increase role column length
ALTER TABLE users
MODIFY COLUMN role VARCHAR(30) NOT NULL;

-- ====================================================================
-- 4. TICKETS Table
-- ====================================================================
-- Check ticket type and status columns
SELECT
    COLUMN_NAME,
    COLUMN_TYPE,
    IS_NULLABLE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'neonhs_db'
  AND TABLE_NAME = 'tickets'
  AND COLUMN_NAME IN ('ticket_type', 'status');

-- Fix: Increase enum columns length (if table exists)
ALTER TABLE tickets
MODIFY COLUMN ticket_type VARCHAR(30);

ALTER TABLE tickets
MODIFY COLUMN status VARCHAR(30);

-- ====================================================================
-- 5. TRANSACTIONS Table
-- ====================================================================
SELECT
    COLUMN_NAME,
    COLUMN_TYPE,
    IS_NULLABLE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'neonhs_db'
  AND TABLE_NAME = 'transactions'
  AND COLUMN_NAME = 'status';

-- Fix (if table exists)
ALTER TABLE transactions
MODIFY COLUMN status VARCHAR(30);

-- ====================================================================
-- 6. VOUCHERS Table
-- ====================================================================
SELECT
    COLUMN_NAME,
    COLUMN_TYPE,
    IS_NULLABLE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'neonhs_db'
  AND TABLE_NAME = 'vouchers'
  AND COLUMN_NAME IN ('voucher_type', 'status');

-- Fix (if table exists)
ALTER TABLE vouchers
MODIFY COLUMN voucher_type VARCHAR(30);

ALTER TABLE vouchers
MODIFY COLUMN status VARCHAR(30);

-- ====================================================================
-- 7. EVENTS Table
-- ====================================================================
SELECT
    COLUMN_NAME,
    COLUMN_TYPE,
    IS_NULLABLE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'neonhs_db'
  AND TABLE_NAME = 'events'
  AND COLUMN_NAME = 'status';

-- Fix (if table exists)
ALTER TABLE events
MODIFY COLUMN status VARCHAR(30);

-- ====================================================================
-- 8. BLOGS Table
-- ====================================================================
SELECT
    COLUMN_NAME,
    COLUMN_TYPE,
    IS_NULLABLE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'neonhs_db'
  AND TABLE_NAME = 'blogs'
  AND COLUMN_NAME = 'status';

-- Fix (if table exists)
ALTER TABLE blogs
MODIFY COLUMN status VARCHAR(30);

-- ====================================================================
-- Verification: Check all enum columns in database
-- ====================================================================
SELECT
    TABLE_NAME,
    COLUMN_NAME,
    COLUMN_TYPE,
    CHARACTER_MAXIMUM_LENGTH
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'neonhs_db'
  AND COLUMN_NAME IN ('status', 'role', 'ticket_type', 'voucher_type')
  AND DATA_TYPE = 'varchar'
ORDER BY TABLE_NAME, COLUMN_NAME;

-- ====================================================================
-- End of migration script
-- ====================================================================
