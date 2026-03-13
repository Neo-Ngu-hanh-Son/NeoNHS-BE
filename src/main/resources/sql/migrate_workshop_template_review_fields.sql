-- Migration: Merge approve/reject tracking fields into unified review fields
-- Run this script ONCE on your database before restarting the application.

-- 1. Rename 'reject_reason' -> 'admin_note'
ALTER TABLE workshop_templates
    CHANGE COLUMN reject_reason admin_note TEXT NULL COMMENT 'Note from admin when approving or rejecting the template';

-- 2. Merge 'approved_by' and 'rejected_by' -> 'reviewed_by'
--    Copy approved_by into a new reviewed_by column first
ALTER TABLE workshop_templates
    ADD COLUMN reviewed_by BINARY(16) NULL COMMENT 'UUID of the admin who approved or rejected this template';

UPDATE workshop_templates
SET reviewed_by = COALESCE(approved_by, rejected_by)
WHERE reviewed_by IS NULL;

ALTER TABLE workshop_templates
    DROP COLUMN approved_by,
    DROP COLUMN rejected_by;

-- 3. Rename 'approved_at' -> 'reviewed_at'
ALTER TABLE workshop_templates
    CHANGE COLUMN approved_at reviewed_at DATETIME(6) NULL COMMENT 'Timestamp when the admin reviewed (approved or rejected) this template';

-- Verify result
DESCRIBE workshop_templates;
