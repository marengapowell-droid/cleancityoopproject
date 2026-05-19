-- ============================================
-- CleanCity - Supabase PostgreSQL Schema
-- ============================================

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================
-- ENUMS
-- ============================================

CREATE TYPE user_role AS ENUM ('RESIDENT', 'COLLECTOR', 'ADMIN');
CREATE TYPE pickup_status AS ENUM ('SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED');
CREATE TYPE report_status AS ENUM ('PENDING', 'IN_PROGRESS', 'RESOLVED', 'REJECTED');
CREATE TYPE waste_type AS ENUM ('PLASTIC', 'ORGANIC', 'ELECTRONIC', 'GENERAL', 'RECYCLABLE');
CREATE TYPE issue_type AS ENUM ('MISSED_PICKUP', 'OVERFLOWING_BIN', 'ILLEGAL_DUMPING', 'DAMAGED_BIN', 'OTHER');

-- ============================================
-- TABLES
-- ============================================

-- Users table (authentication and profile data)
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    role user_role NOT NULL DEFAULT 'RESIDENT',
    is_active BOOLEAN DEFAULT true,
    is_verified BOOLEAN DEFAULT false,
    avatar_url TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP WITH TIME ZONE
);

-- Residents table (extended user data for residents)
CREATE TABLE residents (
    id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    area VARCHAR(100),
    address TEXT,
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    pickup_count INTEGER DEFAULT 0,
    report_count INTEGER DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Collectors table (extended user data for collectors)
CREATE TABLE collectors (
    id UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    zone VARCHAR(100) NOT NULL,
    vehicle_number VARCHAR(50),
    license_number VARCHAR(50),
    assigned_pickups INTEGER DEFAULT 0,
    completed_pickups INTEGER DEFAULT 0,
    rating DECIMAL(3, 2) DEFAULT 0.00,
    is_available BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Zones table (for managing collection zones)
CREATE TABLE zones (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    collector_capacity INTEGER DEFAULT 5,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Pickups table
CREATE TABLE pickups (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    resident_id UUID NOT NULL REFERENCES residents(id) ON DELETE CASCADE,
    collector_id UUID REFERENCES collectors(id) ON DELETE SET NULL,
    zone_id INTEGER REFERENCES zones(id) ON DELETE SET NULL,
    waste_type waste_type NOT NULL,
    location TEXT NOT NULL,
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    scheduled_date DATE NOT NULL,
    scheduled_time TIME NOT NULL,
    status pickup_status DEFAULT 'SCHEDULED',
    notes TEXT,
    image_url TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    assigned_at TIMESTAMP WITH TIME ZONE,
    completed_at TIMESTAMP WITH TIME ZONE
);

-- Reports table
CREATE TABLE reports (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    resident_id UUID NOT NULL REFERENCES residents(id) ON DELETE CASCADE,
    issue_type issue_type NOT NULL,
    location TEXT NOT NULL,
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    description TEXT NOT NULL,
    status report_status DEFAULT 'PENDING',
    image_url TEXT,
    admin_notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP WITH TIME ZONE,
    resolved_by UUID REFERENCES users(id) ON DELETE SET NULL
);

-- Notifications table
CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(50) NOT NULL, -- 'PICKUP', 'REPORT', 'SYSTEM', 'ALERT'
    is_read BOOLEAN DEFAULT false,
    data JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Audit log table
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100),
    entity_id UUID,
    old_values JSONB,
    new_values JSONB,
    ip_address INET,
    user_agent TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Refresh tokens table for JWT
CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token VARCHAR(500) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    revoked_at TIMESTAMP WITH TIME ZONE,
    is_revoked BOOLEAN DEFAULT false
);

-- ============================================
-- INDEXES
-- ============================================

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_residents_area ON residents(area);
CREATE INDEX idx_collectors_zone ON collectors(zone);
CREATE INDEX idx_pickups_resident_id ON pickups(resident_id);
CREATE INDEX idx_pickups_collector_id ON pickups(collector_id);
CREATE INDEX idx_pickups_status ON pickups(status);
CREATE INDEX idx_pickups_scheduled_date ON pickups(scheduled_date);
CREATE INDEX idx_reports_resident_id ON reports(resident_id);
CREATE INDEX idx_reports_status ON reports(status);
CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_is_read ON notifications(is_read);
CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);

-- ============================================
-- TRIGGERS
-- ============================================

-- Update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_residents_updated_at BEFORE UPDATE ON residents
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_collectors_updated_at BEFORE UPDATE ON collectors
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_zones_updated_at BEFORE UPDATE ON zones
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_pickups_updated_at BEFORE UPDATE ON pickups
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_reports_updated_at BEFORE UPDATE ON reports
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Update pickup count on resident
CREATE OR REPLACE FUNCTION update_resident_pickup_count()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        UPDATE residents SET pickup_count = pickup_count + 1 WHERE id = NEW.resident_id;
    ELSIF TG_OP = 'DELETE' THEN
        UPDATE residents SET pickup_count = pickup_count - 1 WHERE id = OLD.resident_id;
    END IF;
    RETURN NULL;
END;
$$ language 'plpgsql';

CREATE TRIGGER trigger_update_resident_pickup_count
    AFTER INSERT OR DELETE ON pickups
    FOR EACH ROW EXECUTE FUNCTION update_resident_pickup_count();

-- Update report count on resident
CREATE OR REPLACE FUNCTION update_resident_report_count()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        UPDATE residents SET report_count = report_count + 1 WHERE id = NEW.resident_id;
    ELSIF TG_OP = 'DELETE' THEN
        UPDATE residents SET report_count = report_count - 1 WHERE id = OLD.resident_id;
    END IF;
    RETURN NULL;
END;
$$ language 'plpgsql';

CREATE TRIGGER trigger_update_resident_report_count
    AFTER INSERT OR DELETE ON reports
    FOR EACH ROW EXECUTE FUNCTION update_resident_report_count();

-- Update collector pickup counts
CREATE OR REPLACE FUNCTION update_collector_pickup_counts()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'UPDATE' AND OLD.status != 'COMPLETED' AND NEW.status = 'COMPLETED' THEN
        UPDATE collectors 
        SET completed_pickups = completed_pickups + 1 
        WHERE id = NEW.collector_id;
    END IF;
    RETURN NULL;
END;
$$ language 'plpgsql';

CREATE TRIGGER trigger_update_collector_pickup_counts
    AFTER UPDATE ON pickups
    FOR EACH ROW EXECUTE FUNCTION update_collector_pickup_counts();

-- ============================================
-- INITIAL DATA
-- ============================================

-- Insert default zones
INSERT INTO zones (name, description, collector_capacity) VALUES
    ('CBD', 'Central Business District', 10),
    ('Westlands', 'Westlands Area', 8),
    ('Kilimani', 'Kilimani Area', 8),
    ('Eastlands', 'Eastlands Area', 12),
    ('Karen', 'Karen Area', 6),
    ('Lavington', 'Lavington Area', 6),
    ('Parklands', 'Parklands Area', 8);

-- ============================================
-- FUNCTIONS
-- ============================================

-- Function to get user statistics
CREATE OR REPLACE FUNCTION get_user_statistics(user_id UUID)
RETURNS JSON AS $$
DECLARE
    stats JSON;
BEGIN
    SELECT json_build_object(
        'total_pickups', COALESCE((SELECT COUNT(*) FROM pickups WHERE resident_id = user_id), 0),
        'completed_pickups', COALESCE((SELECT COUNT(*) FROM pickups WHERE resident_id = user_id AND status = 'COMPLETED'), 0),
        'pending_pickups', COALESCE((SELECT COUNT(*) FROM pickups WHERE resident_id = user_id AND status = 'SCHEDULED'), 0),
        'total_reports', COALESCE((SELECT COUNT(*) FROM reports WHERE resident_id = user_id), 0),
        'resolved_reports', COALESCE((SELECT COUNT(*) FROM reports WHERE resident_id = user_id AND status = 'RESOLVED'), 0)
    ) INTO stats;
    RETURN stats;
END;
$$ LANGUAGE plpgsql;

-- Function to get zone statistics
CREATE OR REPLACE FUNCTION get_zone_statistics(zone_id INTEGER)
RETURNS JSON AS $$
DECLARE
    stats JSON;
BEGIN
    SELECT json_build_object(
        'total_pickups', COALESCE((SELECT COUNT(*) FROM pickups WHERE zone_id = zone_id), 0),
        'completed_pickups', COALESCE((SELECT COUNT(*) FROM pickups WHERE zone_id = zone_id AND status = 'COMPLETED'), 0),
        'pending_pickups', COALESCE((SELECT COUNT(*) FROM pickups WHERE zone_id = zone_id AND status = 'SCHEDULED'), 0),
        'active_collectors', COALESCE((SELECT COUNT(*) FROM collectors WHERE zone = (SELECT name FROM zones WHERE id = zone_id) AND is_available = true), 0)
    ) INTO stats;
    RETURN stats;
END;
$$ LANGUAGE plpgsql;
