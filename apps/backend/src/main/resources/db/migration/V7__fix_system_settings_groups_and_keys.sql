-- =============================================================================
-- V7: Fix system_settings group names and key names to match application code
-- =============================================================================
-- Problem: V1 seeded maintenance_mode under group 'general' but the service
--          and frontend both query group 'system'. SMTP keys also lacked the
--          'smtp_' prefix that the frontend SMTP form expects.
-- Strategy: UPDATE existing rows (UNIQUE constraint allows key rename via UPDATE),
--           INSERT missing rows with IF NOT EXISTS guard for idempotency.
-- =============================================================================

-- 1. Move maintenance_mode from group 'general' to 'system'
UPDATE system_settings
SET    setting_group = 'system'
WHERE  setting_group = 'general'
  AND  setting_key   = 'maintenance_mode';

-- 2. Rename SMTP keys to have smtp_ prefix (matches frontend SMTP_FIELDS array)
UPDATE system_settings
SET    setting_key = 'smtp_host'
WHERE  setting_group = 'smtp' AND setting_key = 'host';

UPDATE system_settings
SET    setting_key = 'smtp_port'
WHERE  setting_group = 'smtp' AND setting_key = 'port';

UPDATE system_settings
SET    setting_key = 'smtp_username'
WHERE  setting_group = 'smtp' AND setting_key = 'username';

UPDATE system_settings
SET    setting_key = 'smtp_from_name'
WHERE  setting_group = 'smtp' AND setting_key = 'from_email';

-- 3. Add missing SMTP settings (smtp_password masked by service, smtp_secure for TLS)
IF NOT EXISTS (
    SELECT 1 FROM system_settings
    WHERE setting_group = 'smtp' AND setting_key = 'smtp_password'
)
    INSERT INTO system_settings (setting_group, setting_key, setting_value, value_type, is_editable)
    VALUES ('smtp', 'smtp_password', N'', 'string', 1);

IF NOT EXISTS (
    SELECT 1 FROM system_settings
    WHERE setting_group = 'smtp' AND setting_key = 'smtp_secure'
)
    INSERT INTO system_settings (setting_group, setting_key, setting_value, value_type, is_editable)
    VALUES ('smtp', 'smtp_secure', N'STARTTLS', 'string', 1);

-- 4. Add missing security settings expected by SecurityTab frontend
IF NOT EXISTS (
    SELECT 1 FROM system_settings
    WHERE setting_group = 'security' AND setting_key = 'lockout_duration_minutes'
)
    INSERT INTO system_settings (setting_group, setting_key, setting_value, value_type, is_editable)
    VALUES ('security', 'lockout_duration_minutes', N'15', 'integer', 1);

IF NOT EXISTS (
    SELECT 1 FROM system_settings
    WHERE setting_group = 'security' AND setting_key = 'jwt_expiry_minutes'
)
    INSERT INTO system_settings (setting_group, setting_key, setting_value, value_type, is_editable)
    VALUES ('security', 'jwt_expiry_minutes', N'60', 'integer', 1);

-- 5. Add system group metadata settings (platform info moved from 'general')
IF NOT EXISTS (
    SELECT 1 FROM system_settings
    WHERE setting_group = 'system' AND setting_key = 'platform_name'
)
    INSERT INTO system_settings (setting_group, setting_key, setting_value, value_type, is_editable)
    VALUES ('system', 'platform_name', N'SakuJi JLPT Platform', 'string', 1);

IF NOT EXISTS (
    SELECT 1 FROM system_settings
    WHERE setting_group = 'system' AND setting_key = 'allow_registration'
)
    INSERT INTO system_settings (setting_group, setting_key, setting_value, value_type, is_editable)
    VALUES ('system', 'allow_registration', N'true', 'boolean', 1);

GO

PRINT N'V7: system_settings groups and keys aligned with application layer.';
GO
