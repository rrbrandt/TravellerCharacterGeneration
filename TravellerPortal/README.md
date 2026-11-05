# Classic Traveller Books 0–8 portal

The current DoxyLabs site uses the static files in [public](public/). It links Book 0, the Books 1–5 tools, and account sign-in. The [dist](dist/) directory holds the home-server version.

Static menu hosted on Tomcathedral's existing Tomcat service at `/traveller/`. Links to the separate character generator8082 and ship builder8083, including their saved-work libraries. Books1 and2 are highlighted because those are the installed applications; this portal does not claim to implement every book.

Source: `dist/index.html`. Deployed file: `C:\Server\apache-tomcat-11.0.27\webapps\traveller\index.html`. Character and ship data remain in their own application directories. The home-server address is currently192.168.1.187; update the four links if its address changes.

Public deployment completed 2026-11-02:
Portal: https://traveller.doxylabs.net/
Ship designer: https://traveller.doxylabs.net/shipyard/
Character generator remains at /traveller/.
Ship service traveller-shipyard uses loopback 8084; persistent data /var/lib/traveller-ships; executable /opt/traveller-shipyard/ship-builder.war. Enabled on boot, separate user and storage. Nginx backup /var/backups/traveller-portal-20261102T145730Z/nginx-traveller. Public portal static files /var/www/traveller-portal; public source TravellerPortal/public/index.html. Existing Hex Frontier on 8083 and character service/data preserved. Existing traffic guard applies to both new routes.
Verified public HTTP routes, portal desktop/mobile/navigation, all eight craft passenger/maneuver references, saved ship restore in fresh browser context, Scout benchmark. Removed exact verification fixture. Dynamic craft customization deferred by user.

Public deployment completed 2026-11-02:
World Builder: https://traveller.doxylabs.net/worlds/
Portal: https://traveller.doxylabs.net/ (three application cards)
Service traveller-worldbuilder, loopback 8086, separate system user.
Persistent server atlases: /var/lib/traveller-atlases; executable /opt/traveller-worldbuilder/world-builder.war.
Release SHA256: 924CB7D3AD569DCEF887BA6FAD26ED7A937B90D0DAB2E26B327963CB22AEB4A0.
Config and portal rollback backup: /var/backups/traveller-worldbuilder-20261102T193528Z.
Cookie-only session tracking prevents session IDs in form URLs. Production cookies Secure.
Public mode labels saved atlases as shared DoxyLabs data; private account ownership is not enabled.
No local atlases were uploaded. Local preview remains at localhost:18084 with its own saved files.
Public map/edit/save/update/fresh-browser/export/CSRF validation passed; exact fixture removed.
Portal Google verification preserved; sitemap includes /worlds/ and robots excludes atlas-library/export crawling.
Existing character, ship, account, Hex Frontier services and traffic guard retained.

2026-11-02: Added Main portal return link to all three public Traveller apps and their saved libraries, via Nginx snippet /etc/nginx/snippets/traveller-main-portal-link.conf. Included only in proxy locations for ports 8082 (characters), 8084 (ships), 8086 (worlds). This avoids application restarts and preserves active Java sessions. Link points to / on the same public host. Source: deploy/main-portal-link.conf; deploy/add-main-portal-links.sh. Rollback config: /var/backups/traveller-portal-links-20261102T195928Z/nginx-traveller. Browser checks passed for exactly one link on all six app/library routes, desktop/mobile visibility, navigation back to the three-card portal, and no JavaScript errors. Local previews and Tomcathedral routing were not changed.

2026-11-02: Published original Book 0 informational introduction at https://traveller.doxylabs.net/book0/. Source public/book0/index.html. Conversational overview, tabletop roles, age-of-sail communications analogy, original starter scenario, and links to Books 1–3 tools. Explicitly informational; no book text reproduced, forms, new game mechanics or login requirement. Notes that Book 0 itself compares its setting with eighteenth-century Earth while Richard's seventeenth-century analogy conveys the same delayed-communications idea. Portal Book 0 marker is clickable with a Start here link; sitemap updated. New static Nginx location preserves three app portal-return snippets and traffic guard. No Java restarts. Rollback /var/backups/traveller-book0-20261102T200601Z. Browser checks passed for informational content, all three app links, return navigation, canonical URL and desktop/tablet/mobile layout; desktop screenshot visually reviewed.
