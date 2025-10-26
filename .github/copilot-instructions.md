<!-- Copilot instructions for the Railway (Steam 'n' Rails) codebase -->
# Quick guide for AI coding agents (updated)

This repository has been ported to NeoForge for Minecraft 1.21.x. The build has already been made successful on the current branch; the current focus is on running and testing the mod in the NeoForge dev environment (runClient / runServer) rather than resolving build issues.

Keep these concise, repo-specific guidelines in mind when making further changes or PRs.

Key points
- Project layout: `common/` contains the canonical game logic, registries, and most source. `neoforge/` contains NeoForge platform glue and run configurations.
- Primary goal now: verify runtime behavior in the NeoForge dev environment (`:neoforge:runClient` / `:neoforge:runServer`) and iterate on runtime fixes or feature ports for 1.21.x.
- Datagen: If you need to regenerate resources, use the NeoForge datagen run (see `neoforge` run configurations). Datagen writes into `common/src/generated/resources` (if needed).

Build & run checklist (what to do now)
1. Confirm the build is up to date (already done). If you need to re-run the build locally, use the Gradle wrapper from the repo root.
   - Preferred: `./gradlew :common:build :neoforge:build`
2. Run the NeoForge client for runtime testing and manual QA:
   - `./gradlew :neoforge:runClient` — this launches the NeoForge dev environment for interactive testing.
3. When making runtime changes that affect registries, data generation, access wideners or resources, run the appropriate datagen task and confirm `common/src/generated/resources` is refreshed (only when required):
   - Set `DATAGEN=TRUE` and run the `:neoforge:runData` or the configured datagen task for the repo.

What to remove/avoid
- This repo is NeoForge-only on the 1.21.* line. Do not add Fabric- or multi-loader-specific instructions or changes unless explicitly requested.

Important conventions and pointers
- Registrate & data-gen: `common/` is the canonical source for registration and data generation. If you add or change registrations, update the data generators in `common` as needed.
- Access widener: `common/src/main/resources/railways.accesswidener` is the single authoritative AW file — do not duplicate AWs across platforms.
- Gradle properties: Use keys in `gradle.properties` (for optional compat toggles or version bumps) instead of hard-coding values.
- Platform adapters: Keep platform-agnostic logic in `common`. Only add code to `neoforge/` if it's platform-specific (entrypoints, mixins wiring, run configs).

Testing & debugging tips
- Use `./gradlew :neoforge:runClient` to test features in-game. The runs in `neoforge/runs/` may already contain helpful local state when debugging.
- If you change registries or data, run datagen and then a quick `:common:compileJava` / `:neoforge:compileJava` as needed to catch compile errors early.
- If a runtime error appears, re-run `:neoforge:runClient` with `--stacktrace` or run Gradle with `--scan` when more detail is needed.

If you need to make additional changes (tests, data-gen updates, or small runtime fixes), prefer small incremental commits that keep the build green.

If you'd like, I can:
- Add a short `README_RUNDEV.md` with the exact `runClient` and `runData` steps used in this repo.
- Run `:neoforge:runClient` locally in this environment to verify the dev environment launches (requires interactive environment access).

Quick references
- `./gradlew :neoforge:runClient` — run the NeoForge client
- `./gradlew :neoforge:runServer` — run the NeoForge server
- `./gradlew :neoforge:runData` (or set `DATAGEN=TRUE`) — run data generation into `common/src/generated/resources`

Short completion summary
- This file replaces earlier, broader porting notes. It focuses on the current repo state: build successful, next steps are runtime testing with NeoForge (`runClient`). Keep changes small and run `:neoforge:runClient` to validate runtime behavior.
