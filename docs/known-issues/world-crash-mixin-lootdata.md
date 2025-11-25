# World creation crash: MixinLootDataManager (1.21.1)

## Summary
Creating a new world (pressing "Create New World" / opening fresh world screen) crashed due to a failing mixin injection in `MixinLootDataManager` targeting the synthetic method `lambda$scheduleElementParse$4` inside `ReloadableServerResources`.

Minecraft 1.21.1 refactored loot data reload scheduling and the targeted lambda no longer exists (different ordinal/name after compilation). The mixin failed validation and caused a critical `InvalidInjectionException`, aborting class transformation during the UI action.

## Crash Signature
```
InvalidInjectionException: could not find any targets matching 'lambda$scheduleElementParse$4' in net/minecraft/server/ReloadableServerResources
Config: railways.mixins.json : MixinLootDataManager
```

## Resolution (Permanent Fix)
The underlying synthetic lambda no longer exists in 1.21.1 (verified by inspecting the decompiled `ReloadableServerResources.java`), so the selective loot skipping hook is obsolete. The brittle mixin was removed. A future selective-load feature will be reintroduced using data pack conditions or a stable reload listener instead of a lambda injection.

If a future need arises to conditionally skip loot table loading, prefer one of:
- Data pack conditional loading (e.g. optional JSON resources gated by tags or by mod presence).
- A mixin targeting a stable named API (avoid synthetic lambda names which change between versions).
- Runtime filtering after tag application (safe post-load pruning) with warning logs instead of cancellation.

## Why Removal Is Correct
- Target method absent: no stable insertion point exists; reintroducing a lambda-targeted injection would reintroduce fragility.
- Behavior impact minimal: skipped loot tables were optional compat track drops; absence has no critical gameplay or stability impact.
- Maintenance: eliminates a crash vector and reduces upgrade friction for future MC versions.

## Follow-up (Optional)
Audit generated loot tables for compat tracks; if any should be suppressed when a compat mod is missing, convert them to conditional recipes or loot modifiers keyed off mod presence rather than injection.

## References
- Removed entry: `MixinLootDataManager` in `railways.mixins.json`
- Removed source file: `neoforge/src/main/java/com/railwayteam/railways/neoforge/mixin/MixinLootDataManager.java`
- Removed helper method: `mixinSkipLootLoading` in `TrackCompatUtils`
- Added robust camera tracking mixin: `conductor_possession.CameraChunkTrackingMixin` replacing outdated chunk tracking logic.

---
Last updated: 2025-11-24
