-- Initial database schema for TOTP sender
-- Created: 2025-06-07 12:00:00

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Example table for future use
-- CREATE TABLE users (
--     id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
--     username VARCHAR(255) NOT NULL UNIQUE,
--     email VARCHAR(255) NOT NULL UNIQUE,
--     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--     updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
-- );
