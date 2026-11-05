-- MySQL 8.0.16+. Run as a schema administrator, never as the runtime account.
-- All application timestamps are UTC. Tokens are SHA-256 digests, never raw links.
CREATE DATABASE traveller CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_cs;
USE traveller;
CREATE TABLE schema_migrations (
  version INT PRIMARY KEY,
  description VARCHAR(255) NOT NULL,
  applied_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
) ENGINE=InnoDB;
CREATE TABLE accounts (
  id CHAR(36) CHARACTER SET ascii COLLATE ascii_bin PRIMARY KEY,
  email VARCHAR(254) NOT NULL,
  email_key VARCHAR(254) COLLATE utf8mb4_0900_bin NOT NULL,
  status ENUM('pending','active','disabled') NOT NULL DEFAULT 'pending',
  verified_at DATETIME(6) NULL,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  UNIQUE KEY uq_accounts_email_key (email_key),
  CONSTRAINT ck_accounts_active_verified CHECK (status <> 'active' OR verified_at IS NOT NULL)
) ENGINE=InnoDB;
CREATE TABLE login_tokens (
  token_hash BINARY(32) PRIMARY KEY,
  account_id CHAR(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
  purpose ENUM('verify_email','sign_in') NOT NULL,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  expires_at DATETIME(6) NOT NULL,
  consumed_at DATETIME(6) NULL,
  KEY ix_tokens_account (account_id),
  KEY ix_tokens_expiry (expires_at),
  CONSTRAINT fk_tokens_account FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE
) ENGINE=InnoDB;
CREATE TABLE auth_sessions (
  session_hash BINARY(32) PRIMARY KEY,
  account_id CHAR(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  last_seen_at DATETIME(6) NOT NULL,
  expires_at DATETIME(6) NOT NULL,
  revoked_at DATETIME(6) NULL,
  KEY ix_sessions_account (account_id),
  KEY ix_sessions_expiry (expires_at),
  CONSTRAINT fk_sessions_account FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE
) ENGINE=InnoDB;
CREATE TABLE saved_work_owners (
  application ENUM('character','ship') NOT NULL,
  record_id VARCHAR(128) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
  account_id CHAR(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (application, record_id),
  KEY ix_owners_account (account_id, application),
  CONSTRAINT fk_owners_account FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE RESTRICT
) ENGINE=InnoDB;
CREATE TABLE registration_notifications (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  account_id CHAR(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
  state ENUM('pending','sending','sent','failed') NOT NULL DEFAULT 'pending',
  attempts INT UNSIGNED NOT NULL DEFAULT 0,
  available_at DATETIME(6) NOT NULL,
  lease_until DATETIME(6) NULL,
  sent_at DATETIME(6) NULL,
  last_error_code VARCHAR(100) NULL,
  created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  UNIQUE KEY uq_registration_notice (account_id),
  KEY ix_notice_delivery (state, available_at),
  CONSTRAINT fk_notice_account FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE
) ENGINE=InnoDB;
INSERT INTO schema_migrations(version,description) VALUES(1,'Initial Traveller identity and ownership foundation');
