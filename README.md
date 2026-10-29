# Traveller Character Generator 1.0

Version **1.0.0** is the CatCathedral character generator, prepared for DoxyLabs.

## Included in 1.0

Six generation tabs: Identity, Characteristics, Service, Skills, Muster Out Benefits,
and Final Character. Characteristics stay in a horizontal grid with classic UPP.
Each browser session keeps its own character and career history. Skill allowances,
commission/promotion rolls, rank bonuses, reenlistment and service-specific
mustering-out benefits are enforced on the server. The final page lists identity,
characteristics, accumulated skill levels, advancement, rewards, and career history.

## Build and run

Use JDK 11 or newer and Maven 3.6+ (or the included Maven wrapper).

```sh
./mvnw test package
java -jar target/TravellerCharacterGeneration.war --server.port=8082
```

Open http://localhost:8082/newcharacter. Final records are at `/character`.
`/version.json` reports the release version. Characters are held in memory for the
browser session; restarting the service clears active sessions.

Four rule-test suites run during Maven testing. The optional browser check is
`node verification/final-character.cjs http://localhost:8082` after installing
Playwright and creating `verification/screenshots/`.

## Release notes and limits

The prior repository implementation is preserved under `archive/version-0.2.0/`.
It is not compiled into 1.0. This release follows the tested CatCathedral flow.
Survival and aging rolls, pensions, and weapon-specialization selection are not
implemented in that flow. Unspecialized combat awards retain their category.
Weapon and ship muster-out awards are recorded without a separate item-choice UI.

## Deployment

Deploy the executable WAR using the existing Traveller service on DoxyLabs. Inspect
its current systemd configuration, port, and context path first; back up the live
WAR and roll back if health checks fail. The DoxyLabs homepage is a different
repository. Do not replace other applications or certificates.

`deploy/install-release.sh` installs an already-built WAR into an existing systemd
service with backup, version checks, and rollback. Pass the inspected service unit,
WAR path, and loopback base URL; it does not change Nginx or certificates. DoxyLabs
uses a `/traveller` application path; start the WAR with
`--server.servlet.context-path=/traveller` when required by its existing service.
The application supports both root and prefixed deployment paths.

For browser verification install Playwright and Chromium, then run:

```sh
npm install --no-save playwright
npx playwright install chromium
mkdir -p verification/screenshots
node verification/final-character.cjs http://localhost:8082
```

Set `PLAYWRIGHT_CHANNEL=msedge` to use an existing Microsoft Edge installation.

## Traffic protection

`deploy/traffic_guard.py install --monthly-gib 100` (root) installs:

- Traveller request limits: 2 requests/second per IP with a burst of 20, and
  30 requests/second per hostname with a burst of 60.
- Eight concurrent requests per IP and 40 per hostname; excess requests get 429.
- A 32 KiB request-body limit and shorter timeouts.
- Outbound-interface monitoring every 30 seconds, with an 80% warning in the
  systemd journal and a Traveller-only 503 pause at the configured threshold.
- Coverage for the Traveller subdomain and other active Nginx locations proxying
  the same application. Existing unrelated applications and certificates stay intact.

The default cutoff is **100 GiB per UTC calendar month**, measured from installation.
The counter includes the entire default-route interface (other apps and private
traffic included), so it is deliberately conservative. Initial installation does
not reconstruct earlier traffic. Usage survives reboots. The pause automatically
clears in the next UTC month. Warnings are in server logs, not emailed.

This is **not a provider-enforced billing cap**: traffic before installation,
monitoring delay, existing transfers, TLS overhead, small rejection responses,
and other services can still incur charges. Check the provider's transfer allowance
and billing independently. The guard does not change DigitalOcean billing alerts.

```sh
/usr/local/sbin/traveller-traffic-guard status
journalctl -u traveller-traffic-guard.service --no-pager -n 20
```

Configuration is `/var/lib/traveller-traffic/config.json`; after changing the
monthly limit, run `/usr/local/sbin/traveller-traffic-guard tick` to apply it.
The installer preserves the existing limit when rerun. Nginx files are backed up
under `/var/backups/traveller-traffic-*` with a manifest. Failed installation
restores the previous Nginx configuration before returning an error.

Run `python3 verification/traffic_guard_test.py` for the traffic-ledger and
Nginx-scope selection tests.

### Saved characters

Use **Save character** on any stage to save a draft or completed character, and **Saved characters** to reopen it. Reopened drafts retain their progress and completed-stage locks. Saving again updates the same entry; starting a new character creates a separate save. Unsaved edits stay only in the browser session until saved.

The library is shared by visitors to this server; there are no user accounts. Versioned JSON snapshots preserve identity, characteristics, career state, skills, benefits, history, and stage locks. Files are written atomically outside the WAR in `./data/characters`, relative to the application's working directory. Set `--traveller.storage-dir=/absolute/path` to override. The application account needs write access to that folder. Preserve and back up this directory when updating or moving the application. Storage failures are reported without claiming that a save succeeded.

### Version 1.0 characteristic reference

The Characteristics page has a live reference chart on the right (below the rolls on smaller screens). It explains the six characteristics, shows the current decimal/UPP score, gives a general score reading, and notes the EDU 8+ training requirement. Score bands are roleplaying guidance, not additional rules, degrees, or mechanical bonuses. The page header and footer display version 1.0. Background: Far Future Enterprises, https://www.farfuture.net/Understanding%20Traveller.pdf.

For an existing DoxyLabs installation with persistent storage configured, `deploy/update-reference.sh WAR` updates only the WAR with backup and automatic rollback. It preserves saved characters and the existing systemd storage and traffic-guard configuration.
