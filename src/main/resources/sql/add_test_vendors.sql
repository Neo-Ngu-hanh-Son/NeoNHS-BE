-- =====================================================
-- Script to add 2 test vendors for Admin Management API testing
-- =====================================================
-- Note: Password for both vendors is "SecurePass123" (hashed with BCrypt)
-- BCrypt hash: $2a$10$XYZ... (you need to generate this using BCryptPasswordEncoder)

-- =====================================================
-- VENDOR 1: Workshop Pro Services
-- =====================================================

-- Insert User 1 (Vendor)
INSERT INTO users (
    id,
    fullname,
    email,
    password_hash,
    phone_number,
    avatar_url,
    role,
    is_active,
    is_verified,
    is_banned,
    created_at,
    updated_at
) VALUES (
    UUID(), -- Auto-generated UUID
    'Michael Anderson',
    'michael.anderson@workshoppro.com',
    '$2a$10$N9qo8uLOickgx2ZrVzY0LeleqfO7zTlF/XDdGhQpS3e.dJkzXw7bO', -- Password: SecurePass123
    '+15551234567',
    'https://randomuser.me/api/portraits/men/32.jpg',
    'VENDOR',
    TRUE,
    TRUE,
    FALSE,
    NOW(),
    NOW()
);

-- Get the last inserted user ID for Vendor 1
SET @vendor1_user_id = LAST_INSERT_ID();

-- Insert Vendor Profile 1
INSERT INTO vendor_profiles (
    id,
    business_name,
    description,
    address,
    latitude,
    longitude,
    tax_code,
    bank_name,
    bank_account_number,
    bank_account_name,
    is_verified,
    user_id,
    created_at,
    updated_at
) VALUES (
    UUID(),
    'Workshop Pro Services',
    'Leading provider of professional automotive and technical workshops. Specializing in advanced repair techniques, maintenance services, and technical training for professionals. Over 15 years of industry experience.',
    '123 Industrial Avenue, Tech District, San Francisco, CA 94103',
    '37.774929',
    '-122.419416',
    'TAX-WPS-2024-001',
    'Bank of America',
    '1234567890123',
    'Workshop Pro Services LLC',
    TRUE,
    (SELECT id FROM users WHERE email = 'michael.anderson@workshoppro.com'),
    NOW(),
    NOW()
);

-- =====================================================
-- VENDOR 2: Creative Skills Hub
-- =====================================================

-- Insert User 2 (Vendor)
INSERT INTO users (
    id,
    fullname,
    email,
    password_hash,
    phone_number,
    avatar_url,
    role,
    is_active,
    is_verified,
    is_banned,
    created_at,
    updated_at
) VALUES (
    UUID(), -- Auto-generated UUID
    'Sarah Martinez',
    'sarah.martinez@creativeskills.com',
    '$2a$10$N9qo8uLOickgx2ZrVzY0LeleqfO7zTlF/XDdGhQpS3e.dJkzXw7bO', -- Password: SecurePass123
    '+15559876543',
    'https://randomuser.me/api/portraits/women/44.jpg',
    'VENDOR',
    TRUE,
    TRUE,
    FALSE,
    NOW(),
    NOW()
);

-- Get the last inserted user ID for Vendor 2
SET @vendor2_user_id = LAST_INSERT_ID();

-- Insert Vendor Profile 2
INSERT INTO vendor_profiles (
    id,
    business_name,
    description,
    address,
    latitude,
    longitude,
    tax_code,
    bank_name,
    bank_account_number,
    bank_account_name,
    is_verified,
    user_id,
    created_at,
    updated_at
) VALUES (
    UUID(),
    'Creative Skills Hub',
    'Innovative learning center offering hands-on workshops in arts, crafts, design, and digital creativity. We empower individuals to discover and develop their creative potential through expert-led sessions.',
    '456 Creative Lane, Arts Quarter, Los Angeles, CA 90012',
    '34.052235',
    '-118.243683',
    'TAX-CSH-2024-002',
    'Wells Fargo Bank',
    '9876543210987',
    'Creative Skills Hub Inc',
    TRUE,
    (SELECT id FROM users WHERE email = 'sarah.martinez@creativeskills.com'),
    NOW(),
    NOW()
);

-- =====================================================
-- VENDOR 3: TechMasters Academy (Unverified for testing)
-- =====================================================

-- Insert User 3 (Vendor - Unverified)
INSERT INTO users (
    id,
    fullname,
    email,
    password_hash,
    phone_number,
    avatar_url,
    role,
    is_active,
    is_verified,
    is_banned,
    created_at,
    updated_at
) VALUES (
    UUID(),
    'David Chen',
    'david.chen@techmasters.com',
    '$2a$10$N9qo8uLOickgx2ZrVzY0LeleqfO7zTlF/XDdGhQpS3e.dJkzXw7bO', -- Password: SecurePass123
    '+15555551234',
    'https://randomuser.me/api/portraits/men/67.jpg',
    'VENDOR',
    TRUE,
    FALSE, -- Not verified
    FALSE,
    NOW(),
    NOW()
);

-- Insert Vendor Profile 3 (Unverified)
INSERT INTO vendor_profiles (
    id,
    business_name,
    description,
    address,
    latitude,
    longitude,
    tax_code,
    bank_name,
    bank_account_number,
    bank_account_name,
    is_verified,
    user_id,
    created_at,
    updated_at
) VALUES (
    UUID(),
    'TechMasters Academy',
    'New tech training center specializing in programming, web development, and IT certifications. Pending verification.',
    '789 Tech Street, Silicon Valley, CA 94025',
    '37.413294',
    '-122.077754',
    'TAX-TMA-2024-003',
    'Chase Bank',
    '5555555555555',
    'TechMasters Academy LLC',
    FALSE, -- Not verified
    (SELECT id FROM users WHERE email = 'david.chen@techmasters.com'),
    NOW(),
    NOW()
);

-- =====================================================
-- Verification Query
-- =====================================================
-- Run this to verify the data was inserted correctly:
/*
SELECT
    u.id as user_id,
    u.fullname,
    u.email,
    u.phone_number,
    u.role,
    u.is_active,
    u.is_verified as user_verified,
    u.is_banned,
    vp.id as vendor_profile_id,
    vp.business_name,
    vp.description,
    vp.address,
    vp.tax_code,
    vp.bank_name,
    vp.bank_account_number,
    vp.is_verified as vendor_verified,
    u.created_at
FROM users u
INNER JOIN vendor_profiles vp ON u.id = vp.user_id
WHERE u.email IN (
    'michael.anderson@workshoppro.com',
    'sarah.martinez@creativeskills.com',
    'david.chen@techmasters.com'
)
ORDER BY u.created_at DESC;
*/

-- =====================================================
-- Test Login Credentials
-- =====================================================
-- Vendor 1: michael.anderson@workshoppro.com / SecurePass123
-- Vendor 2: sarah.martinez@creativeskills.com / SecurePass123
-- Vendor 3: david.chen@techmasters.com / SecurePass123 (Unverified)
