# Traveller Shipyard — separate application

Classic Traveller Book 2, **1981 revised edition**, ship-construction preview. Uses Java11+, Spring Boot2.5.10/Spring MVC, Thymeleaf, HTML/CSS/vanilla JavaScript, Jackson JSON persistence, an executable Tomcat WAR, and Maven. It has its own application entry point, routes, port, data folder and UI; it does not modify or run inside the character generator.

## Run

With Java11+ and Maven:

```sh
mvn clean package
java -jar target/TravellerShipBuilder.war
```

Default preview: http://localhost:18083/ (loopback only). Saved ships: `./data/ships`, relative to the working directory. Override with `--shipbuilder.storage-dir=/absolute/path`. Back up that folder; it is outside the WAR. This preview has a shared library and no accounts.

On Richard's existing development machine, `./build-preview.ps1` can compile and package offline using the existing Spring Boot dependency WAR and library directory. Those parameters are configurable; standard Maven builds do not depend on the character-generator checkout. The offline packager removes all of that WAR's application classes and resources and installs only this app's own classes and resources, retaining the Boot loader/dependency jars.

## Features

- 100–5,000-ton standard/custom hulls, separate engineering/main capacity, next-larger-size drive lookup.
- Lettered jump/maneuver drives and power plants, actual Book2 performance table, plant-letter and computer jump-support validation.
- Minimum fuel, optional reserves, bridge, staterooms, low/emergency berths, occupancy and crew checks.
- Hardpoints, single/double/triple turrets, fire-control reserves and four weapon types.
- Published vehicle/small-craft catalogue, cargo allocation, component ledger, costs, standard-plan discount, architect fee and financing.
- Live server-side calculations, explicit recalculation without JavaScript, validation before save, update/open saved designs, JSON download and print worksheet.
- Scout/Courier and Free Trader presets, checked against the book's figures.

## Rules scope and interpretation

Construction data is based on Book2(1981), pp.12–23. Mechanical data and formulas are implemented as calculations; prose is original summary, not a reproduction of the book. The publisher's [Classic Traveller collection](https://www.farfuture.net/) is the recommended rules reference. The [indexed 1981 Book2 text](https://anyflip.com/nculs/sbnj/basic/101-150) was used to cross-check table values and the examples.

This is a construction worksheet, not a space-combat or deck-plan generator. It does not mix in Book5/High Guard rules or design custom small craft. Carrying the fixed published craft is supported; removable cutter modules occupy their own allocation, so do not count a loaded module twice.

Computer hardware jump support is enforced. Detailed flight/combat software selection and CPU/storage scheduling are left to the book; an optional additional-software cost can be entered. Shipyard tech restrictions beyond the displayed computer TL and shipyard class selection are not modelled. Double-role crew is opt-in and assumes qualified personnel/referee-approved pairing. Medic counts use the greater of the ship minimum and one per started120 passengers; optional gunners assume weapons are spread across the available turrets. Craft crews can be added manually to assigned crew. The standard-plan checkbox is an assertion that the design is established and entitled to its discount, not a claim that any new custom ship qualifies.

Custom engineering allocation0 means exactly the drive volume; standard-hull engineering capacity is fixed, so unused engineering cannot become cargo. Fuel/cargo input-1 means automatic minimum/remaining space. Prices are calculated in whole credits; purchase totals include the architect fee for custom plans, while mortgage figures are based on the ship purchase price (excluding that fee).

## Verification

`verification/Book2Check.java` covers published examples and invalid combinations. `verification/browser.cjs` covers live recalculation, invalid-save rejection, save/update, new-session restore, export, invalid IDs and mobile layout. The browser script uses this machine's Playwright installation; adjust its import path on another machine.

Benchmarks:

| Design | Cargo | Fuel | Purchase | Performance |
|---|---:|---:|---:|---|
| Scout/Courier | 3t | 40t | MCr29.43 | J2 / 2G |
| Free Trader | 82t | 30t | MCr37.08 | J1 / 1G |

The separate app is a preview for review. The portal/menu and public hosting are deliberately left for the next stage requested by Richard.

### Selected small-craft reference

Selecting a positive quantity displays a per-craft reference card: listed people (pilot + rider for standard craft, pilot only for the fighter), maneuver G, hull, four-week fuel tankage, excess fitting space and price. The eight Book2 standard designs are covered. These are the base designs from Book2(1981), pp.17–18: most can be operated by one pilot and their passenger capacity depends on how excess space is fitted, not on a fixed passenger count. Couches use0.5 ton each. The cutter's30-ton module bay is distinct from its2.5 tons of additional fitting space. Vehicle quantity0 removes its card. Reference cards do not alter costs, displacement or saved schema.

Passenger counts on each craft card exclude one pilot. The listed-layout passenger count is the existing rider place (one for the two-person craft, zero for the fighter). The seating-only upper bound adds floor(excessSpace/0.5) couches to that listed count. This is a calculated fitting option, not the stock passenger capacity; it consumes all excess space, excludes cutter modules, and assumes no other added crew, cargo, cabins, weapons or fuel. Additional crew use passenger places.
