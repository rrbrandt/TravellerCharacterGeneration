# Classic Traveller Books 0–8 portal

The current DoxyLabs site uses the static files in [public](public/). It links Book 0, Books 1–8 tools, and account sign-in. The [dist](dist/) directory holds the home-server version.

## Book 8 · Robot Workshop

Robot Workshop 1.0 is at `/book8/`. Start from an original utility, grav rescue, or battery-powered station robot. The worksheet uses selected 1986 Book 8 component and application tables to calculate the Universal Robot Profile, volume, loaded mass, power, thrust, endurance, cost, and annual maintenance. It checks software and processor requirements. Designs can be saved in the browser, printed, or downloaded.

The worksheet allocates 1 L per kg to equipment that has no listed volume and fits gear inside the body or head. Battery-only ground drives use a declared transmission rating. Weapon-law prompts call for referee judgment. These are construction worksheets with a selected catalog of components and programs.

## Book 7 · Merchant Prince

Merchant Prince 1.0 is at `/book7/`, with the Trade Desk at `/book7/trade.html`. Careers use the 1985 book’s six line types, enlistment and education, yearly assignments, commission exams, transfers, bonuses and mustering out. Quick roll follows the recruit preferences. Year-by-year play pauses between postings and lets the player decide on optional re-enlistment. Other choices follow the recruit settings.

The Trade Desk takes manual UWP entries or an exported Book 3 atlas or Book 6 survey. It can read the declared hold from a saved ship export or accept available tonnage from a ship worksheet. Freight is loaded in whole lots. Speculative goods use Book 7’s sourceworld pricing, market modifiers and brokered sale roll. This release uses Imperial trade and reconstructs the market pairings from the book’s descriptions where the table scan is ambiguous. Voyage costs are entered by the referee. Résumés and settled voyages can be saved in the browser, printed, or downloaded as JSON.

## Book 6 · Scouts

The local portal displays each implemented book's own starting version, 1.0. Serve `public/` with an HTTP server and open `/book6/` for the Scout career generator. For example, run `python -m http.server 18088 --bind 127.0.0.1 --directory public` from this directory.

Scout careers follow the 1983 Book 6 Field and Bureaucracy procedures with Book 1 aging and mustering out. Quick roll uses the requested term count. Year-by-year mode pauses between assignments and asks whether to re-enlist when the rules allow a choice. Other choices follow the recruit settings. Completed records can be saved in the current browser, printed, or downloaded as JSON.

World Creator 1.0 at `/book6/survey.html` can expand a pasted Book 3 mainworld profile or choose a mainworld in a newly rolled system. It logs dice rolls and retains the generation seed. It estimates orbital zones from representative stellar luminosities. The surface chart is a portal extension, with profile-driven hex terrain and editable site pins on a 20-face map. Surveys, including site edits, are saved in the current browser or downloaded as JSON.

Published on DoxyLabs on 2026-11-05 at https://traveller.doxylabs.net/book6/. Nginx serves the static Book 6 files from `/var/www/traveller-portal/book6`. The public portal displays separate 1.0 badges for Books 0–6. Rollback files are in `/var/backups/traveller-book6-OQIQeLNp/prior`.

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
