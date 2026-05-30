-- Enlarge password_hash column for PBKDF2 hashes (iterations:salt:hash format)
-- MD5 was 32 chars, PBKDF2 is ~120 chars
ALTER TABLE crest_user MODIFY COLUMN password_hash VARCHAR(256);
