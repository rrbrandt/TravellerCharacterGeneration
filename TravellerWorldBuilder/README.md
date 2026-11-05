# Traveller World Builder — local preview 0.1

Book 3 revised 1981 world/subsector generation, matching the Java/Spring Boot 2.5.10,
Thymeleaf, plain CSS/JavaScript and Jackson stack of Traveller Shipyard.

URL: http://localhost:18084/
Library: http://localhost:18084/library
Bound to 127.0.0.1 only. No Tomcathedral, DoxyLabs, public portal or account-service
deployment/configuration was changed. No new cloud database or login required.

Features: single mainworld and 8-by-10 subsector generation, standard 4+ presence
on 1D, sparse/dense thresholds, reproducible seed, keyboard/clickable SVG map,
searchable world list, eight UWP explanations, basic six Classic trade codes,
naval/scout bases, gas giants, dice/modifier history, referee profile edits/notes,
atomic JSON save/update, library, JSON and plain-text downloads, print/PDF layout.

Storage: C:\gitsrc\TravellerWorldBuilder\data\atlases\<UUID>.json
Each saved atlas contains all its worlds and roll history. Existing saved character
and ship files remain independent. No tests or user atlas are pre-saved by default.

Start from this project folder:

    java -Xms32m -Xmx192m -jar target/TravellerWorldBuilder.war

Build with Maven package, or build-preview.ps1 using the existing local character
application dependency WAR. The offline build replaces all application classes and
resources; no character code, ship code or their data is carried into this app.

## Rules decisions

- Revised 1981 mainworlds, not 1977 or Mongoose. Hydrographics is 2D-7+atmosphere,
  with -4 at atmosphere 0/1/A+. This follows the correction to the Book 3 checklist.
- Size 0 forces atmosphere/hydrographics 0. Size 1 does not force hydrographics 0
  under the 1981 formula. Hydrographics is capped at A (100%); negative values floor 0.
- Size is thousands of miles, with approximate kilometre conversion for convenience.
- Raw rolled government/law/tech values are preserved. No modern population-zero
  resets or environmental minimum tech rules are silently introduced.
- High law results use extended hex that skips I/O (20 is L). Codes above the basic
  government/atmosphere descriptions explicitly request referee interpretation;
  Book 6 descriptions are not silently substituted. The law prohibition summary
  stops at basic level 9 while its enforcement throw continues rising.
- Naval bases only A/B, 8+. Scout bases only A-D, 7+ with A -3/B -2/C -1. Gas giants
  present on 2D <=9. Each world's PRNG seed is independent of presence-roll stream,
  so changing density retains existing worlds' profiles at the same coordinates.
- Referee-entered profiles can intentionally differ from generated physical/base
  constraints; no invented dice history is attached to them. No extra modern PBG,
  star types, planet counts or population multipliers are asserted.
- Blank seed makes a fresh random seed. Same edition/settings/seed reproduces the
  survey. Names and planet illustrations are application presentation, not Book 3 rules.
- Scope chosen by Richard: 1981 world/subsector generator. Animal/patron encounters,
  equipment, psionics and expanded Book 6 systems are not part of this version.
- Travel zones/jump routes require referee judgement; neither is fabricated here.
- Descriptions are abbreviated paraphrases to explain codes, not full book text.

Rules cross-check references:
- FFE publisher guide: https://www.farfuture.net/Guide%20to%20Classic%20Traveller.pdf
- Consolidated CT Errata, Book 3 p12 hydrographics correction:
  https://www.codewarriormalo.com/downloads/RPGs/Traveller/Classic%20Traveller/CT%20Errata%20Compendium.pdf
- GDW Classic Traveller Starter Edition world checklist (1981-core formula cross-check)
  and GDW Supplement 3 UWP explanations (size units, basic government/law descriptions).

## Verification

Book3Check.java passes deterministic boundary and fixture checks: all starport roll
boundaries, asteroid exceptions, corrected size-1 hydrographics, high extended law,
raw low-population tech, Regina A788899-C decoding, overlapping trade codes,
invalid profiles/hexes, reproducibility, density independence, and 3,979 seeded worlds.

verification/browser.cjs passed against the local HTTP app in Edge: 80-hex map,
selection, UWP details, dice history, filtering, narrow/mobile layout, single world,
manual edits, local save/update, reopen in a fresh browser, JSON/text export, missing
CSRF, invalid profiles, duplicate hex and traversal-ID rejection. Desktop/mobile
screenshots were generated; desktop visually inspected. Test fixture files removed.

This is a local preview on the older existing application stack. Review/update
dependencies and hosting/security requirements before considering public deployment.

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
