# Traveller Character Generation

A Spring Boot and Thymeleaf application implementing Classic Traveller basic
character generation.

Current release: **0.2.0**

## Requirements

- Java 17 or newer
- No system Maven installation is required; the Maven wrapper is included.

## Run locally

On Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Then open <http://localhost:8080/>.

## Test and package

```powershell
.\mvnw.cmd test
.\mvnw.cmd package
```

The build creates an executable and externally deployable WAR in `target/`.

## Current rules coverage

The app supports character naming, starting-age validation, 2D6
characteristics, UPP display, enlistment and the draft, and all six basic
services. Careers proceed in four-year terms with survival, commission,
promotion, aging, limited skill rolls, reenlistment, and accumulated skill
levels. Characters leaving service receive rank-adjusted mustering-out rolls on
the service-specific cash and material-benefit tables, including retirement pay
and the three-roll cash limit.
