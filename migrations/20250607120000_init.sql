-- Initial database schema for TOTP sender
-- Created: 2025-06-07 12:00:00

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Users table
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    username VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL CHECK (role IN ('ADMIN', 'USER')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- OTP configuration table (should have only one record)
CREATE TABLE otp_config (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    code_length INTEGER NOT NULL DEFAULT 6 CHECK (code_length >= 4 AND code_length <= 8),
    ttl_seconds INTEGER NOT NULL DEFAULT 300 CHECK (ttl_seconds > 0),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- OTP codes table
CREATE TABLE otp_codes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    code VARCHAR(8) NOT NULL,
    operation_id VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'EXPIRED', 'USED')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP
);

-- Indexes for optimization
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_otp_codes_code ON otp_codes(code);
CREATE INDEX idx_otp_codes_expires_at ON otp_codes(expires_at);
CREATE INDEX idx_otp_codes_status ON otp_codes(status);
CREATE INDEX idx_otp_codes_user_id ON otp_codes(user_id);

-- Initial data
-- Default admin user (password: admin123, BCrypt hash)
INSERT INTO users (username, password_hash, role) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSfFGjA77pAed3yv5wJZwQ.rq', 'ADMIN');

-- Default OTP configuration: 6 digits, 5 minutes TTL
INSERT INTO otp_config (code_length, ttl_seconds) VALUES (6, 300);
