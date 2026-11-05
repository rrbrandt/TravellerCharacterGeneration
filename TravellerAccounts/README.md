# Traveller account pages — preview 0.1, 2026-11-02

Public login: https://traveller.doxylabs.net/account/login
Registration: https://traveller.doxylabs.net/account/register
Portal has a Log in / Register link. Google verification tag preserved.

Separate Java/Spring Boot/Thymeleaf app, matching the existing apps' stack. Uses
the new MySQL traveller schema. Service traveller-accounts, loopback 8085,
artifact /opt/traveller-accounts/accounts.war. Nginx account routes preserve existing
traffic protection. Root-owned credentials are provided read-only using systemd
LoadCredential; no credentials are packaged in the WAR or source.

Email delivery is DISABLED. Forms clearly display the pending setup and disable
submission. Direct submission is also rejected, without creating an account or
sending email. Sender and registration notification address are configured as
richraybrandt@gmail.com in root-only /etc/traveller/mail.json; no password exists.
Direct Gmail SMTP over IPv4 port 587 timed out from the droplet. IPv6 was unavailable.
Need a working delivery route (Gmail HTTPS API or HTTPS transactional service are
possible alternatives) and securely configured credentials before activation.
The deployed mail adapter now uses Gmail HTTPS API with refresh-token authorization. See GMAIL-SETUP.md for owner authorization and secure installation. No OAuth credentials have been configured yet.

Implemented, but email-disabled: registration/login link requests, expiring random
32-byte tokens stored as SHA-256 digests, explicit confirmation POST with CSRF,
single-use transactional token consumption, account verification, 24-hour hashed
auth sessions, Secure/HttpOnly/SameSite=Lax cookies, session rotation, logout and
one first-registration notification queue entry. Background notification sender
uses leases/retries (5 attempts), and is inactive without enabled mail. SMTP delivery
can cause duplicate notices on retry if a remote send succeeds before DB update.

Verification link tokens are in URL fragments, cleared client-side before confirmation;
they are not query parameters in proxy access logs. Pages are no-store/noindex with
no-referrer. Login and registration responses avoid disclosing whether an eligible
account exists. Email-key policy is trim + lowercase ASCII; non-ASCII email support
is not implemented. CSRF uses a server-side form session; Nginx traffic limits plus
per-address minute throttling apply. Application has 8 request threads; DB account
allows 10 concurrent connections. Production auth still needs a broader security
review, dependency updates from the existing older Boot stack, token/session cleanup,
recurring tested backups, and testing of actual email delivery before enabling.

These accounts DO NOT yet gate the character/ship apps or privatize their shared
saved libraries. No old work was migrated. That remains a separate integration stage.

Verification passed: desktop/mobile layouts, portal navigation, email-disabled UI
and backend, missing CSRF rejection, expired link, valid confirmation, duplicate
link rejection, active account, secure session cookie, logout revocation, one queued
registration notice. A temporary reserved example.invalid account was seeded for
the real HTTP verification flow, then deleted with all related test records. No emails
were sent. Screenshots were visually inspected. verify.cjs uses paced Edge requests.

Build: standard Maven pom, or build-preview.ps1 using existing dependency WAR plus
official Connector/J 8.4.0, javax.mail 1.6.2, javax.activation 1.2.0 jars in lib/.
Offline builder stores nested jar entries uncompressed, as required by Boot's loader.
Gmail API release SHA256: 0A6D93005997471A606F0646CE0A76BE0C85079C2EC7C889468AE3D22659DA78.
Routing backup: /var/backups/traveller-accounts-routing-20261102T153228Z.

Gmail authorization completed 2026-11-02. Root-only Gmail API OAuth credentials installed through secure SSH stdin by install-gmail.py. Google accepted one setup email to richraybrandt@gmail.com; service health reports emailEnabled=true. Login/registration forms are now enabled. No OAuth token or client secret was printed or saved in source. Actual user registration/email-link click and owner notification still need confirmation. Google Cloud project is in Testing; Gmail-scope refresh tokens normally expire after seven days until an appropriate production publishing configuration is completed. Existing shared apps remain publicly accessible.
