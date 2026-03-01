-- Add rejected_by column to workshop_templates table
-- This column tracks which admin user rejected the workshop template

ALTER TABLE workshop_templates
ADD COLUMN rejected_by BINARY(16) NULL COMMENT 'UUID of the admin who rejected this template';

-- Optional: Add a foreign key constraint if you want referential integrity
-- ALTER TABLE workshop_templates
-- ADD CONSTRAINT fk_workshop_templates_rejected_by
-- FOREIGN KEY (rejected_by) REFERENCES users(id) ON DELETE SET NULL;

-- Verify the column was added
DESCRIBE workshop_templates;
