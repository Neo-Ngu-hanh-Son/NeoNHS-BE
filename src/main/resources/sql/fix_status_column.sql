-- SQL script to fix workshop_templates.status column length issue
-- Run this script manually in your MySQL database
USE neonhs_db;
-- Check current column definition
SHOW COLUMNS FROM workshop_templates LIKE 'status';
-- Alter column to increase length
ALTER TABLE workshop_templates 
MODIFY COLUMN status VARCHAR(20) NOT NULL;
-- Verify the change
SHOW COLUMNS FROM workshop_templates LIKE 'status';
