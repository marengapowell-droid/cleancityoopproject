-- ============================================
-- CleanCity - Row Level Security (RLS) Policies
-- ============================================

-- Enable RLS on all tables
ALTER TABLE users ENABLE ROW LEVEL SECURITY;
ALTER TABLE residents ENABLE ROW LEVEL SECURITY;
ALTER TABLE collectors ENABLE ROW LEVEL SECURITY;
ALTER TABLE zones ENABLE ROW LEVEL SECURITY;
ALTER TABLE pickups ENABLE ROW LEVEL SECURITY;
ALTER TABLE reports ENABLE ROW LEVEL SECURITY;
ALTER TABLE notifications ENABLE ROW LEVEL SECURITY;
ALTER TABLE audit_logs ENABLE ROW LEVEL SECURITY;
ALTER TABLE refresh_tokens ENABLE ROW LEVEL SECURITY;

-- ============================================
-- USERS TABLE POLICIES
-- ============================================

-- Users can view their own profile
CREATE POLICY "Users can view own profile" ON users
    FOR SELECT USING (auth.uid()::text = id::text);

-- Admins can view all users
CREATE POLICY "Admins can view all users" ON users
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM users 
            WHERE id::text = auth.uid()::text 
            AND role = 'ADMIN'
        )
    );

-- Users can update their own profile
CREATE POLICY "Users can update own profile" ON users
    FOR UPDATE USING (auth.uid()::text = id::text)
    WITH CHECK (auth.uid()::text = id::text);

-- Admins can update all users
CREATE POLICY "Admins can update all users" ON users
    FOR UPDATE USING (
        EXISTS (
            SELECT 1 FROM users 
            WHERE id::text = auth.uid()::text 
            AND role = 'ADMIN'
        )
    )
    WITH CHECK (
        EXISTS (
            SELECT 1 FROM users 
            WHERE id::text = auth.uid()::text 
            AND role = 'ADMIN'
        )
    );

-- Admins can delete users
CREATE POLICY "Admins can delete users" ON users
    FOR DELETE USING (
        EXISTS (
            SELECT 1 FROM users 
            WHERE id::text = auth.uid()::text 
            AND role = 'ADMIN'
        )
    );

-- ============================================
-- RESIDENTS TABLE POLICIES
-- ============================================

-- Residents can view their own data
CREATE POLICY "Residents can view own data" ON residents
    FOR SELECT USING (auth.uid()::text = id::text);

-- Collectors can view resident data for their zone
CREATE POLICY "Collectors can view residents in their zone" ON residents
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM collectors c
            JOIN users u ON c.id = u.id
            WHERE u.id::text = auth.uid()::text
            AND c.zone = residents.area
        )
    );

-- Admins can view all residents
CREATE POLICY "Admins can view all residents" ON residents
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM users 
            WHERE id::text = auth.uid()::text 
            AND role = 'ADMIN'
        )
    );

-- Residents can update their own data
CREATE POLICY "Residents can update own data" ON residents
    FOR UPDATE USING (auth.uid()::text = id::text)
    WITH CHECK (auth.uid()::text = id::text);

-- Admins can update all residents
CREATE POLICY "Admins can update all residents" ON residents
    FOR UPDATE USING (
        EXISTS (
            SELECT 1 FROM users 
            WHERE id::text = auth.uid()::text 
            AND role = 'ADMIN'
        )
    )
    WITH CHECK (
        EXISTS (
            SELECT 1 FROM users 
            WHERE id::text = auth.uid()::text 
            AND role = 'ADMIN'
        )
    );

-- ============================================
-- COLLECTORS TABLE POLICIES
-- ============================================

-- Collectors can view their own data
CREATE POLICY "Collectors can view own data" ON collectors
    FOR SELECT USING (auth.uid()::text = id::text);

-- Admins can view all collectors
CREATE POLICY "Admins can view all collectors" ON collectors
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM users 
            WHERE id::text = auth.uid()::text 
            AND role = 'ADMIN'
        )
    );

-- Collectors can update their own data
CREATE POLICY "Collectors can update own data" ON collectors
    FOR UPDATE USING (auth.uid()::text = id::text)
    WITH CHECK (auth.uid()::text = id::text);

-- Admins can update all collectors
CREATE POLICY "Admins can update all collectors" ON collectors
    FOR UPDATE USING (
        EXISTS (
            SELECT 1 FROM users 
            WHERE id::text = auth.uid()::text 
            AND role = 'ADMIN'
        )
    )
    WITH CHECK (
        EXISTS (
            SELECT 1 FROM users 
            WHERE id::text = auth.uid()::text 
            AND role = 'ADMIN'
        )
    );

-- ============================================
-- ZONES TABLE POLICIES
-- ============================================

-- All authenticated users can view zones
CREATE POLICY "All authenticated users can view zones" ON zones
    FOR SELECT USING (auth.uid() IS NOT NULL);

-- Admins can insert zones
CREATE POLICY "Admins can insert zones" ON zones
    FOR INSERT WITH CHECK (
        EXISTS (
            SELECT 1 FROM users 
            WHERE id::text = auth.uid()::text 
            AND role = 'ADMIN'
        )
    );

-- Admins can update zones
CREATE POLICY "Admins can update zones" ON zones
    FOR UPDATE USING (
        EXISTS (
            SELECT 1 FROM users 
            WHERE id::text = auth.uid()::text 
            AND role = 'ADMIN'
        )
    )
    WITH CHECK (
        EXISTS (
            SELECT 1 FROM users 
            WHERE id::text = auth.uid()::text 
            AND role = 'ADMIN'
        )
    );

-- Admins can delete zones
CREATE POLICY "Admins can delete zones" ON zones
    FOR DELETE USING (
        EXISTS (
            SELECT 1 FROM users 
            WHERE id::text = auth.uid()::text 
            AND role = 'ADMIN'
        )
    );

-- ============================================
-- PICKUPS TABLE POLICIES
-- ============================================

-- Residents can view their own pickups
CREATE POLICY "Residents can view own pickups" ON pickups
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM residents r
            WHERE r.id::text = auth.uid()::text
            AND r.id = pickups.resident_id
        )
    );

-- Collectors can view pickups assigned to them
CREATE POLICY "Collectors can view assigned pickups" ON pickups
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM collectors c
            WHERE c.id::text = auth.uid()::text
            AND c.id = pickups.collector_id
        )
    );

-- Collectors can view pickups in their zone
CREATE POLICY "Collectors can view pickups in their zone" ON pickups
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM collectors c
            JOIN users u ON c.id = u.id
            JOIN zones z ON c.zone = z.name
            WHERE u.id::text = auth.uid()::text
            AND z.id = pickups.zone_id
        )
    );

-- Admins can view all pickups
CREATE POLICY "Admins can view all pickups" ON pickups
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM users 
            WHERE id::text = auth.uid()::text 
            AND role = 'ADMIN'
        )
    );

-- Residents can create pickups
CREATE POLICY "Residents can create pickups" ON pickups
    FOR INSERT WITH CHECK (
        EXISTS (
            SELECT 1 FROM residents r
            WHERE r.id::text = auth.uid()::text
            AND r.id = pickups.resident_id
        )
    );

-- Residents can update their own pickups (before assignment)
CREATE POLICY "Residents can update own pickups" ON pickups
    FOR UPDATE USING (
        EXISTS (
            SELECT 1 FROM residents r
            WHERE r.id::text = auth.uid()::text
            AND r.id = pickups.resident_id
            AND pickups.status = 'SCHEDULED'
        )
    )
    WITH CHECK (
        EXISTS (
            SELECT 1 FROM residents r
            WHERE r.id::text = auth.uid()::text
            AND r.id = pickups.resident_id
            AND pickups.status = 'SCHEDULED'
        )
    );

-- Collectors can update assigned pickups
CREATE POLICY "Collectors can update assigned pickups" ON pickups
    FOR UPDATE USING (
        EXISTS (
            SELECT 1 FROM collectors c
            WHERE c.id::text = auth.uid()::text
            AND c.id = pickups.collector_id
        )
    )
    WITH CHECK (
        EXISTS (
            SELECT 1 FROM collectors c
            WHERE c.id::text = auth.uid()::text
            AND c.id = pickups.collector_id
        )
    );

-- Admins can update all pickups
CREATE POLICY "Admins can update all pickups" ON pickups
    FOR UPDATE USING (
        EXISTS (
            SELECT 1 FROM users 
            WHERE id::text = auth.uid()::text 
            AND role = 'ADMIN'
        )
    )
    WITH CHECK (
        EXISTS (
            SELECT 1 FROM users 
            WHERE id::text = auth.uid()::text 
            AND role = 'ADMIN'
        )
    );

-- Admins can delete pickups
CREATE POLICY "Admins can delete pickups" ON pickups
    FOR DELETE USING (
        EXISTS (
            SELECT 1 FROM users 
            WHERE id::text = auth.uid()::text 
            AND role = 'ADMIN'
        )
    );

-- ============================================
-- REPORTS TABLE POLICIES
-- ============================================

-- Residents can view their own reports
CREATE POLICY "Residents can view own reports" ON reports
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM residents r
            WHERE r.id::text = auth.uid()::text
            AND r.id = reports.resident_id
        )
    );

-- Admins can view all reports
CREATE POLICY "Admins can view all reports" ON reports
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM users 
            WHERE id::text = auth.uid()::text 
            AND role = 'ADMIN'
        )
    );

-- Residents can create reports
CREATE POLICY "Residents can create reports" ON reports
    FOR INSERT WITH CHECK (
        EXISTS (
            SELECT 1 FROM residents r
            WHERE r.id::text = auth.uid()::text
            AND r.id = reports.resident_id
        )
    );

-- Residents can update their own reports (before resolution)
CREATE POLICY "Residents can update own reports" ON reports
    FOR UPDATE USING (
        EXISTS (
            SELECT 1 FROM residents r
            WHERE r.id::text = auth.uid()::text
            AND r.id = reports.resident_id
            AND reports.status = 'PENDING'
        )
    )
    WITH CHECK (
        EXISTS (
            SELECT 1 FROM residents r
            WHERE r.id::text = auth.uid()::text
            AND r.id = reports.resident_id
            AND reports.status = 'PENDING'
        )
    );

-- Admins can update all reports
CREATE POLICY "Admins can update all reports" ON reports
    FOR UPDATE USING (
        EXISTS (
            SELECT 1 FROM users 
            WHERE id::text = auth.uid()::text 
            AND role = 'ADMIN'
        )
    )
    WITH CHECK (
        EXISTS (
            SELECT 1 FROM users 
            WHERE id::text = auth.uid()::text 
            AND role = 'ADMIN'
        )
    );

-- Admins can delete reports
CREATE POLICY "Admins can delete reports" ON reports
    FOR DELETE USING (
        EXISTS (
            SELECT 1 FROM users 
            WHERE id::text = auth.uid()::text 
            AND role = 'ADMIN'
        )
    );

-- ============================================
-- NOTIFICATIONS TABLE POLICIES
-- ============================================

-- Users can view their own notifications
CREATE POLICY "Users can view own notifications" ON notifications
    FOR SELECT USING (auth.uid()::text = user_id::text);

-- Users can update their own notifications (mark as read)
CREATE POLICY "Users can update own notifications" ON notifications
    FOR UPDATE USING (auth.uid()::text = user_id::text)
    WITH CHECK (auth.uid()::text = user_id::text);

-- System can insert notifications (handled by backend)
CREATE POLICY "System can insert notifications" ON notifications
    FOR INSERT WITH CHECK (true);

-- Users can delete their own notifications
CREATE POLICY "Users can delete own notifications" ON notifications
    FOR DELETE USING (auth.uid()::text = user_id::text);

-- ============================================
-- AUDIT LOGS TABLE POLICIES
-- ============================================

-- Admins can view all audit logs
CREATE POLICY "Admins can view audit logs" ON audit_logs
    FOR SELECT USING (
        EXISTS (
            SELECT 1 FROM users 
            WHERE id::text = auth.uid()::text 
            AND role = 'ADMIN'
        )
    );

-- Users can view their own audit logs
CREATE POLICY "Users can view own audit logs" ON audit_logs
    FOR SELECT USING (auth.uid()::text = user_id::text);

-- System can insert audit logs (handled by backend)
CREATE POLICY "System can insert audit logs" ON audit_logs
    FOR INSERT WITH CHECK (true);

-- ============================================
-- REFRESH TOKENS TABLE POLICIES
-- ============================================

-- Users can view their own refresh tokens
CREATE POLICY "Users can view own refresh tokens" ON refresh_tokens
    FOR SELECT USING (auth.uid()::text = user_id::text);

-- Users can insert their own refresh tokens
CREATE POLICY "Users can insert own refresh tokens" ON refresh_tokens
    FOR INSERT WITH CHECK (auth.uid()::text = user_id::text);

-- Users can revoke their own refresh tokens
CREATE POLICY "Users can revoke own refresh tokens" ON refresh_tokens
    FOR UPDATE USING (auth.uid()::text = user_id::text)
    WITH CHECK (auth.uid()::text = user_id::text);

-- Users can delete their own refresh tokens
CREATE POLICY "Users can delete own refresh tokens" ON refresh_tokens
    FOR DELETE USING (auth.uid()::text = user_id::text);

-- ============================================
-- SECURITY FUNCTIONS
-- ============================================

-- Function to check if user is admin
CREATE OR REPLACE FUNCTION is_admin()
RETURNS BOOLEAN AS $$
BEGIN
    RETURN EXISTS (
        SELECT 1 FROM users 
        WHERE id::text = auth.uid()::text 
        AND role = 'ADMIN'
    );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Function to check if user is resident
CREATE OR REPLACE FUNCTION is_resident()
RETURNS BOOLEAN AS $$
BEGIN
    RETURN EXISTS (
        SELECT 1 FROM users 
        WHERE id::text = auth.uid()::text 
        AND role = 'RESIDENT'
    );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Function to check if user is collector
CREATE OR REPLACE FUNCTION is_collector()
RETURNS BOOLEAN AS $$
BEGIN
    RETURN EXISTS (
        SELECT 1 FROM users 
        WHERE id::text = auth.uid()::text 
        AND role = 'COLLECTOR'
    );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;
