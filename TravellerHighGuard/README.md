# Classic Traveller High Guard — local preview 0.1

Separate **Book 5, revised 1980** ship-construction application. Local preview: http://localhost:18086/ . Java 11+, Spring Boot 2.5.10 / Spring MVC, Thymeleaf, vanilla JavaScript/CSS, Jackson JSON and an executable Tomcat WAR. The original Book 2 app, source and saved ships are unchanged. Published on DoxyLabs at https://traveller.doxylabs.net/highguard/ on 2026-11-03. The local preview remains available.

## Build and run

Standard build: `mvn clean package`, then `java -jar target/TravellerHighGuard-0.1.0.war` from this folder. `build-preview.ps1` also builds offline using the existing character-generator dependency WAR and library directory, replacing all application classes/resources with this app's own. It writes `target/TravellerHighGuard.war`; `-OutputWar` allows packaging while another WAR is running. It requires the local JDK 25 compiler with `--release 11`; the app itself runs on Java 11 or newer.

The default server binds **127.0.0.1 only**, port 18086. Launch background Java with a hidden window. Saved High Guard ships live in `./data/ships` outside the WAR. Override with `--highguard.storage-dir=...`. The read-only local Book 2 library defaults to `C:/gitsrc/TravellerShipBuilder/data/ships`, configurable through `--highguard.book2-dir=...`; upload exported Book 2 JSON if a source is on DoxyLabs. The two data directories may not overlap.

## Included

- Hulls 100–1,000,000 displacement tons; TL7–15; nine configurations including planetoids.
- Custom High Guard jump/maneuver drives, scalable power plants, fuel scoops, purification, internal fuel and disposable external tanks.
- Bridge and backup bridges, computers with fib/bis variants, ship-size/jump/TL checks.
- Armor, meson screens, nuclear dampers, referee-approved TL15 black globes and extra capacitors.
- All 36 spinal selections (18 particle / 18 meson), 50/100-ton bays, empty bays, turret/barbette weapon groups, battery factors and bearings, shared hull weapon allocations and energy validation.
- Crew sections, military/civilian quarters, troops, passengers, low berths, frozen watch, carried craft and vehicles, launch tubes, cargo and mission fittings.
- Live server-rendered reports, explicit Recalculate without JavaScript, full component ledger, USP, save/update/save-copy, library, JSON export, printable worksheet.
- Patrol escort, jump-6 courier and heavy cruiser starter designs; original starter examples, not claimed reproductions of published ships.

## Book 2 transfers

Import accepts the **schemaVersion 1 Book 2 saved-export wrapper**. Source designs are checked with an isolated snapshot of the original Book 2 calculator in `highguard/book2/`. The original application is not linked to or changed. Import creates an unsaved High Guard design with an empty ID. It stores the original JSON, source name and SHA-256 in the new record; Download original returns that snapshot. Local source files are opened read-only. The High Guard save path can never target the Book 2 directory.

Performance ratings transfer, but drives are rebuilt using High Guard percentages and prices. Fuel is recalculated; original cargo remains a target; quarters, crew, cost and technology must be reviewed. Weapons become homogeneous turret groups because the Book 2 export does not record weapon-by-weapon turret placement. An explicit conversion acknowledgement is required before saving. Source craft retain their Book 2 purchase values as carried references and receive High Guard space allowances. Conversions may need adjustment to fit; import does not certify equivalence. Returning a High Guard design to Book 2 is outside this release.

## Rules reference and conventions

Checked against **High Guard (1980), pp. 20–36**: [primary text reproduction](https://anyflip.com/nculs/sbnj/basic/251-300). Descriptions/UI are original; numeric mechanics are implemented as calculations. The rules engine is Classic Traveller, not Mongoose High Guard.

- Percentage drive, armor and fuel calculations retain fractions. Crew/headcount and required rooms round upward; agility rounds down; batteries bearing round to nearest, with one battery per installed weapon category always bearing.
- Bay weapons use their highest available component at/below ship TL. A blank later TL row retains the earlier component rather than creating a new bonus. Each bay adds its shell price (MCr0.01/ton) to its weapon price.
- Hull weapon allocation follows the printed 50,000t example: spinal displacement + 1,000 hull tons per bay + 100 hull tons per turret. Reserved weapon allocation and actual interior displacement are separate constraints.
- Turret groups are homogeneous, with 1–10 identical mounts per battery and equal USP factors within a category. Mixed individual turrets are not supported. Duplicate category groups are combined for bearing rounding. Unused bays are empty reservations; occupying them with cargo or craft at 50% wastage is not automated.
- Over 1,000t: command starts at 11 personnel; above 20,000t it is at least 5 per 10,000t, rounded upward. Engineering is one per 100 drive tons. Gunnery includes the chief, type specialists, mount crews and four personnel per screen. Flights include declared craft crews, one maintainer per craft, a controller and ten crew per tube. Vehicle support is added above three vehicles. Service uses 2/1,000t with troops, 3/1,000t without.
- Up to 1,000t: separate Book 1/2 crew roles; no dual-role qualification assumption. Passengers are assigned stewards conservatively. The worksheet assigns one individual room per occupied section head (plus troop commander), with remaining military crew sharing; civilian crew/passengers have individual rooms. Add mission personnel explicitly.
- Carried small craft use their own tons up to 1,000t, 130% above; big craft use 110%. Dispersed hulls use external attachment without extra fittings. Surface equipment uses its actual tonnage. This is a carried-craft allocation, not a new internal small-craft designer.
- Retained drop-tank performance derives from installed High Guard drive percentages at increased displacement; normal ratings resume after jettison. The added tank price is separate and replacement recurs after use. Purification sizes against internal fuel; arrange external refining for external tank loads.
- Black globes require TL15 and an explicit referee permission checkbox; artifact acquisition is not treated as ordinary shopping. Extra capacitors add to the storage already present in jump drives. The app does not simulate black-globe combat.
- The final fighter-squadron USP position remains 0; fighter organization and combat are deferred. Plant numbers beyond the printed single-character range (above 33) display bracketed decimal values. These extensions are explained on the report.
- Fuel purification TL15 price follows the printed chart (Cr150,000 per 1,000t), despite its non-monotonic price. No silent errata or later-edition substitutions.

Fleet combat, internal small-craft designs and deck plans are future work.

## Naval character generation

Open http://localhost:18086/naval-career.html from the local High Guard preview. The browser rolls a complete career using the recruit settings, logs individual assignments and dice, and can print or download the result as JSON. The career uses the 1980 Book 5 naval rules and Book 1 Navy benefits.

The generator applies player preferences automatically. It uses the selected gunnery specialty, Handgun and Cutlass for weapon choices, and a TL-based vehicle default. Enter the homeworld and subsector capital tech levels to determine service availability. It treats unresolvable branch draws as a valid fallback and stops an exceptionally long active career after 30 terms.

Save character stores a snapshot under `naval-careers/` in the configured High Guard storage directory. The browser remembers links to characters saved there. A saved link can reopen the record in another browser and should be shared only with intended readers.

## Persistence and verification

High Guard saves carry `schemaVersion=1`, `ruleset=CT-HG1980`, UTC save time, and design. Writes use a temporary file, fsync and atomic replace where supported. Invalid designs cannot overwrite valid saves; Save as new copy assigns a fresh UUID. CSRF checks protect all POSTs; bindable fields are allowlisted and counts bounded. The local library is shared, with no user accounts. Keep the data directory backed up. On DoxyLabs, records are shared with visitors; the UI says so. The service is loopback-only behind HTTPS Nginx, has a dedicated user and state directory, secure session cookies, a 160 MB heap limit, and a read-only mount of Book 2 storage. Imports may also use uploaded Book 2 JSON.

`verification/HighGuardCheck.java` covers published fuel/armor/battery/bearing/hardpoint examples, presets, specializations of hull and equipment rules, original preservation on import, invalid inputs and 1,000 varied designs. Compile it against `target/classes` and the existing dependency jars, then run `com.javasmyths.highguard.HighGuardCheck`.

`verification/browser.cjs` runs real Edge through live calculations, valid save/copy, invalid-save rejection, presets, dynamic armament/craft, removal of all weapons, Book 2 import and acknowledgement, source snapshot export, cross-session restore, CSRF/numeric errors, mobile layout, print-to-PDF and no-JavaScript recalculation. Screenshots and a worksheet PDF are created under verification/. Adjust the Playwright dependency path on other machines.


## DoxyLabs deployment

Public route: https://traveller.doxylabs.net/highguard/ . systemd service: traveller-highguard, internal port 8088, executable /opt/traveller-highguard/highguard.war, saves /var/lib/traveller-highguard. Portal and sitemap include Book 5; Book 2 is still /shipyard/. Naval archive deployment backup: /var/backups/traveller-highguard-archive-MnjwUdXx. Published WAR SHA-256: B0C0909E7FC1D704AB050E8F0EB6EF8B97C9B1500125E364BB655331192CEB61.

Public browser checks passed saving, cross-session restore, invalid-save rejection, live calculation, Book 2 upload/import with unchanged source snapshot, mobile layout and both portal links. All seven Traveller/nginx services reported active after deployment. Local save files were not uploaded.
