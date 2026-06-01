-- UC-35 BR-35-05: track wrong TOTP entries per TFA_TEMP token (max 5 before revoke)
ALTER TABLE auth_tokens ADD mfa_attempts INT NOT NULL DEFAULT 0;
