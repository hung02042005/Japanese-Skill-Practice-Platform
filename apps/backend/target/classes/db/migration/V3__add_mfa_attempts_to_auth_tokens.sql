-- UC-35 BR-35-05: track wrong TOTP entries per TFA_TEMP token (max 5 before revoke)
IF NOT EXISTS (
    SELECT 1 FROM sys.columns
    WHERE object_id = OBJECT_ID('auth_tokens') AND name = 'mfa_attempts'
)
    ALTER TABLE auth_tokens ADD mfa_attempts INT NOT NULL DEFAULT 0;
