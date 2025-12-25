This repository has been ported to NeoForge for Minecraft 1.21.x. The build has already been made successful on the current branch; the current focus is on running and testing the mod in the NeoForge dev environment (runClient / runServer) rather than resolving build issues.

Keep these concise, repo-specific guidelines in mind when making further changes or PRs.

Key points
- Project layout: `src/` contains the canonical game logic, registries, and source code at the root level. This is a unified NeoForge-only structure for 1.21.x.
- Primary goal now: verify runtime behavior in the NeoForge dev environment (runClient / runServer) and iterate on runtime fixes or feature ports for 1.21.x.
- Datagen: If you need to regenerate resources, run the datagen task. Datagen writes into `src/generated/resources` and `build/` directories as needed.

Build & run checklist (what to do now)
1. Confirm the build is up to date (already done). If you need to re-run the build locally, use the Gradle wrapper from the repo root.
   - Preferred: `./gradlew build`
2. Run the NeoForge client for runtime testing and manual QA:
   - `./gradlew runClient` — this launches the NeoForge dev environment for interactive testing.
3. When making runtime changes that affect registries, data generation, access wideners or resources, run the appropriate datagen task and confirm `src/generated/resources` is refreshed (only when required):
   - Set `DATAGEN=TRUE` and run `./gradlew runData` or the configured datagen task for the repo.

What to remove/avoid
- This repo is NeoForge-only on the 1.21.* line. Do not add Fabric- or multi-loader-specific instructions or changes unless explicitly requested.

Important conventions and pointers
- Registrate & data-gen: `src/` is the canonical source for registration and data generation. If you add or change registrations, update the data generators in `src/` as needed.
- Access widener: `src/main/resources/railways.accesswidener` is the single authoritative AW file — do not duplicate AWs.
- Gradle properties: Use keys in `gradle.properties` (for optional compat toggles or version bumps) instead of hard-coding values.
- Code organization: Keep all platform-agnostic and NeoForge-specific logic in the unified `src/` structure (no platform splits).

Testing & debugging tips
- Use `./gradlew runClient` to test features in-game. The run in `run/` may already contain helpful local state when debugging.
- If you change registries or data, run datagen and then a quick `./gradlew compileJava` as needed to catch compile errors early.
- If a runtime error appears, re-run `./gradlew runClient` with `--stacktrace` or run Gradle with `--scan` when more detail is needed.

If you need to make additional changes (tests, data-gen updates, or small runtime fixes), prefer small incremental commits that keep the build green.

Quick references
- `./gradlew runClient` — run the NeoForge client
- `./gradlew runServer` — run the NeoForge server
- `./gradlew runData` (set `DATAGEN=TRUE`) — run data generation into `src/generated/resources`

Short completion summary
- This file reflects the unified NeoForge 1.21.x architecture. Build and runtime are single-step operations at the repo root. Keep changes small and run `./gradlew runClient` to validate runtime behavior.
