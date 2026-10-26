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
