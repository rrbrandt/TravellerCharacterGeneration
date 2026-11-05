# Traveller Mercenary — local preview 0.1

Separate Java 11 / Spring Boot 2.5.10 / Thymeleaf application for Classic Traveller Book 4 Army and Marine careers. Local URL: http://localhost:18085/ . No public deployment is part of this preview.

Build with `./build-preview.ps1` using the existing Traveller dependency WAR and libraries. Run the resulting executable WAR with Java 11 or later from this project directory. `application.properties` binds the server to loopback only. Do not expose this preview publicly: saved records are shared and there is no authentication.

Features: Book 1 enlistment/draft; 4 annual assignments per term (training is the first year); MOS and rank-based skill tables; combat wounds, decorations, ranks; special schools and refresher/instructor postings; OCS; cross-training and term-boundary transfers; aide assignment choice; ageing and crises; re-enlistment and mandatory natural 12; cash/material benefits; local automatic save/resume; JSON export; full record and employer résumé print views.

Original Book 4 table convention: the optional general assignment DM uses EDU 8+, despite the prose saying INT 8+. Initial careers support service-world TL 5–15. Recruiting units, mercenary tickets, equipment purchasing, battle resolution and importing Book 1 characters are outside this first career-generator stage. Equipment specialties are categories; use the referee's available equipment list for a specific world's inventory. Material gun/blade benefits are recorded as equipment awards rather than assigned weapon models.

Records are atomic JSON saves under `data/characters/`. They include the complete pending state and random-generator state. Server-side CSRF and revision checks reject old form submissions and forbidden stage changes. Names and history are escaped by Thymeleaf. Save data stays on the local machine; keep this directory backed up if records matter.

Verification: `verification/RulesCheck.java` runs 1,000 deterministic careers across Army and Marines, checking termination, training, rank bounds and selected specialty/table conditions. `verification/browser.cjs` exercises real Edge forms, automatic saves, reopen, stale-form rejection, Marine arm selection and phone-width overflow. Screenshots are under verification/. Offline build uses the dependency libraries already installed for the character generator.

Rules reference checked: Classic Traveller Book 4 Mercenary, military character generation pp. 3–15, with Book 1 core procedures. UI descriptions are original. Unofficial fan project.
