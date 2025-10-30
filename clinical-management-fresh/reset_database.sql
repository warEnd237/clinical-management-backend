-- WARNING: This will delete ALL data in the database
-- Only use in development!

-- Drop all tables if they exist (in correct order to handle foreign keys)
DROP TABLE IF EXISTS refresh_tokens CASCADE;
DROP TABLE IF EXISTS audit_logs CASCADE;
DROP TABLE IF EXISTS notifications CASCADE;
DROP TABLE IF EXISTS messages CASCADE;
DROP TABLE IF EXISTS invoices CASCADE;
DROP TABLE IF EXISTS prescriptions CASCADE;
DROP TABLE IF EXISTS appointments CASCADE;
DROP TABLE IF EXISTS doctors CASCADE;
DROP TABLE IF EXISTS patients CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- Drop the flyway schema history to start fresh
DROP TABLE IF EXISTS flyway_schema_history CASCADE;

-- Drop the extension
DROP EXTENSION IF EXISTS btree_gist;

-- Drop the function
DROP FUNCTION IF EXISTS update_updated_at_column() CASCADE;
