# Owner-only Gmail API setup

Status: Gmail API adapter deployed with email disabled. Google OAuth credentials
are not yet available. DigitalOcean documents that SMTP ports 25/465/587 are blocked.
The droplet reaches oauth2.googleapis.com and gmail.googleapis.com over HTTPS.

1. Create a Google Cloud project named DoxyLabs Traveller Mail, signed in as
   richraybrandt@gmail.com. Keep the generated project ID.
2. In that project, enable Gmail API in APIs & Services > Library.
3. Configure Google Auth Platform branding/audience. Use an External audience for
   a personal Gmail account. Use Richard's email as support/developer contact.
   During initial testing, add richraybrandt@gmail.com as a test user.
4. Create an OAuth client of type Desktop app, named Traveller Mail Setup.
   Download its JSON to Richard's PC. Do not paste the JSON or credentials in chat.
5. Run on Richard's PC (requires Node and his existing authorized DoxyLabs SSH key):

   node C:\gitsrc\TravellerAccounts\connect-gmail.cjs "C:\path\to\downloaded-client.json"

   This launches the normal browser. Richard signs in and reviews consent himself.
   Scope: gmail.send plus openid/email to confirm the owner mailbox. No inbox read
   or delete permission is requested. PKCE and a random state protect the loopback
   callback; setup expires after ten minutes. Client JSON stays at the chosen path.
   OAuth refresh credentials are sent directly via SSH stdin, never CLI arguments,
   browser storage or a local refresh-token file.
6. The root-only server installer rechecks the mailbox identity using Google's API,
   sends one setup email to Richard, backs up the prior config, installs mode-0600
   OAuth credentials, restarts the service and verifies readiness. If activation
   fails it restores the previous mail configuration. Check the setup email inbox.
7. Verify an actual registration email, click its link, confirm sign-in, check the
   owner notification and logout. Then evaluate gating application routes and
   associating saved work with accounts. Existing apps remain public until then.

Important: Google refresh tokens from an External OAuth project in Testing normally
expire in seven days when Gmail scopes are requested. This is suitable for initial
validation, not unattended production. Follow Google's publishing/verification
requirements or choose a transactional email provider before relying on delivery.
Only Richard authorizes the sender mailbox; site visitors do not grant Gmail access.

Local GmailApiCheck validates successful refresh/send and MIME construction, approved
sender, missing credentials, disabled configuration and safe error handling with
fake transport responses. It does not prove actual Gmail delivery without consent.

Official references:
- https://docs.digitalocean.com/support/why-is-smtp-blocked/
- https://developers.google.com/workspace/gmail/api/auth/scopes
- https://developers.google.com/identity/protocols/oauth2/resources/loopback-migration
- https://developers.google.com/identity/protocols/oauth2

Gmail authorization completed 2026-11-02. Root-only Gmail API OAuth credentials installed through secure SSH stdin by install-gmail.py. Google accepted one setup email to richraybrandt@gmail.com; service health reports emailEnabled=true. Login/registration forms are now enabled. No OAuth token or client secret was printed or saved in source. Actual user registration/email-link click and owner notification still need confirmation. Google Cloud project is in Testing; Gmail-scope refresh tokens normally expire after seven days until an appropriate production publishing configuration is completed. Existing shared apps remain publicly accessible.
