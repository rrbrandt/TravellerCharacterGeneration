# Traveller database stage — 2026-11-02

Installed on DoxyLabs (147.182.181.185), using existing MySQL 8.0.46.
Database: `traveller`, InnoDB, utf8mb4. Migration: V001.

The public apps are not connected to these tables yet. No registration, authentication,
email delivery, or saved-file migration is enabled. Existing character and ship JSON
libraries remain unchanged. No existing records were assigned to an account.

Tables: accounts, login_tokens, auth_sessions, saved_work_owners,
registration_notifications, schema_migrations.

`traveller_app@localhost` has only SELECT/INSERT/UPDATE/DELETE on the five runtime
tables and SELECT on schema_migrations, with a 10-connection limit. It has no schema
or account management privileges. MySQL binds to loopback only. Root-readable
credentials stay exclusively on the server in `/etc/traveller/database.json` and
`/etc/traveller/database-client.cnf`, mode 0600. Do not copy credentials into Git,
browser files, logs, or command arguments. Future services need controlled credential
access; current character/ship services cannot read these root-only files.

Application rules required in the next stage:

- Preserve email for display; compute email_key consistently (trim and lowercase
  under an explicit case-insensitive account policy; normalize domain IDNA).
  Never strip dots or plus suffixes. Schema does not validate mailbox syntax.
- Use UUID account IDs. Use random, high-entropy sign-in tokens and session IDs;
  store only 32-byte SHA-256 digests. Use UTC for all DATETIME values and DB sessions.
- Enforce active/verified status in all authentication and authorization paths.
- Consume a token with a conditional UPDATE requiring not consumed and not expired,
  check affected row count, and create the session in the same transaction.
- Queue the first-registration notice in the activation transaction. Unique account
  key prevents duplicate queue entries. Mail sender needs retry/lease handling;
  external email delivery must account for possible retries/duplicate delivery.
- Ownership is a reference to existing application record IDs, not a copy of saved
  content. Check ownership on every read/write/download. Unclaimed legacy records
  remain separate until an explicit migration/claim policy is selected.
- Implement expiry cleanup, session revocation, secure cookies, rate limits, CSRF
  protection, and account deletion/ownership retention policy before enabling login.
- No passwords are stored; this schema prepares passwordless email sign-in.

Provisioning: `provision.py` applies `V001__accounts.sql` as root and generates a
MySQL random password without printing it. Refuses an existing database/account;
it is intentionally not a reset/re-run script. MySQL DDL is not transactionally
rolled back; inspect any partially applied setup before repair.

Verification: `verify.py` runs against the runtime account, using rollback-only
fixtures. Passed duplicate-email, verification-state, foreign-key, token single-use
update, ownership/session/notification insertion, notification uniqueness, runtime
privilege and credential-file permission checks. No fixture accounts remain.

Initial root-only backup: `/var/backups/traveller-database/initial-v001.sql`.
This is an initial snapshot, not scheduled backups. Before accepting registrations,
configure recurring encrypted/off-server backups and test restore recovery.
No database port was opened publicly, and no other databases were modified.
