-- Check if users were created
SELECT id, email, full_name, role, is_active 
FROM users 
ORDER BY id;

-- Check password hashes (for debugging)
SELECT email, password_hash 
FROM users 
WHERE email IN ('admin@clinical.com', 'doctor@clinical.com', 'secretary@clinical.com');
