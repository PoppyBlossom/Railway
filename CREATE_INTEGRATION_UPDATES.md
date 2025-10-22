# Create Mod Integration Updates for 1.21.1

This document summarizes the changes made to update Create mod integrations for Minecraft 1.21.1 with NeoForge.

## Changes Made

### 1. Catnip Services API Update
**File**: `forge/src/main/java/com/railwayteam/railways/content/fuel/tank/FuelTankRenderer.java`

**Change**: Replaced `ForgeCatnipServices` with `CatnipServices`
- Old: `import net.createmod.catnip.platform.ForgeCatnipServices;`
- New: `import net.createmod.catnip.platform.CatnipServices;`
- Updated: `ForgeCatnipServices.FLUID_RENDERER.renderFluidBox(...)` → `CatnipServices.FLUID_RENDERER.renderFluidBox(...)`

**Reason**: Catnip unified the platform services API. The `ForgeCatnipServices` class was merged into the common `CatnipServices` interface, removing the need for platform-specific service classes.

### 2. NeoForge 1.21 Pack API Update
**File**: `forge/src/main/java/com/railwayteam/railways/forge/RailwaysClientImpl.java`

**Changes**:
- Uncommented and updated imports for NeoForge 1.21 Pack API
- Replaced the old Pack API with the new one:
  - `Pack.Info` → `PackLocationInfo` + `PackSelectionConfig`
  - `ModFilePackResources` → `PathPackResources`
  - New `Pack.readMetaAndCreate()` method with `ResourcesSupplier` pattern

**Old API (1.20.1)**:
```java
Pack.create(
    Railways.asResource(pack.id).toString(),
    Component.literal(pack.name),
    false,
    (a) -> new ModFilePackResources(pack.name, modFile, "resourcepacks/" + pack.id),
    new Pack.Info(Component.empty(), 10, FeatureFlagSet.of()),
    PackType.CLIENT_RESOURCES,
    Pack.Position.TOP,
    false,
    PackSource.DEFAULT
)
```

**New API (1.21.1)**:
```java
PackLocationInfo packInfo = new PackLocationInfo(
    Railways.asResource(pack.id).toString(),
    Component.literal(pack.name),
    PackSource.BUILT_IN,
    java.util.Optional.empty()
);

PathPackResources packResources = new PathPackResources(packInfo, resourcePath);

PackSelectionConfig selectionConfig = new PackSelectionConfig(
    false,  // required
    Pack.Position.TOP,
    false   // fixedPosition
);

Pack.readMetaAndCreate(
    packInfo,
    new Pack.ResourcesSupplier() {
        @Override
        public PathPackResources openPrimary(PackLocationInfo info) {
            return packResources;
        }

        @Override
        public PathPackResources openFull(PackLocationInfo info, Pack.Metadata metadata) {
            return packResources;
        }
    },
    PackType.CLIENT_RESOURCES,
    selectionConfig
);
```

**Key Changes**:
- Pack metadata is now separated into `PackLocationInfo` and `PackSelectionConfig`
- Resource packs are now created via `PathPackResources` instead of `ModFilePackResources`
- Pack creation uses a `ResourcesSupplier` pattern instead of a simple factory function
- Feature flags are no longer part of pack creation (handled elsewhere)

### 3. TrainPacket Stub Enhancement
**File**: `forge/src/main/java/com/simibubi/create/content/trains/entity/TrainPacket.java`

**Changes**:
- Added fields: `train`, `trainId`
- Added methods: constructors, `write()`, `handle()`
- Added documentation

**Reason**: The stub needs to expose enough of the TrainPacket API for the mixins to compile. The actual implementation comes from the Create mod at runtime.

### 4. TrainPacket Mixin Documentation
**Files**:
- `forge/src/main/java/com/railwayteam/railways/mixin/MixinTrainPacket.java`
- `forge/src/main/java/com/railwayteam/railways/mixin/client/MixinTrainPacket.java`

**Changes**: Added comments explaining:
- The purpose of each injection point
- Why `ordinal = 1` is used
- The fragility of lambda method names
- Potential debugging steps if mixins fail

## Potential Issues and Testing

### Known Fragile Points

1. **Lambda Method Name** (`client.MixinTrainPacket`):
   - Targets: `lambda$handle$0`
   - Risk: Compiler-generated names change with code refactoring
   - Testing: Check if JourneyMap train markers update correctly when trains are removed
   - Alternative: Consider targeting the `handle()` method directly or using @ModifyVariable

2. **Ordinal Injection Points** (`MixinTrainPacket`):
   - Uses `ordinal = 1` for both `write()` and constructor injection
   - Risk: If Create adds/removes return statements, ordinals may need adjustment
   - Testing: Verify handcar status is correctly synchronized between client and server

### Testing Checklist

When this builds successfully:

- [ ] Verify pack registration works (builtin resource packs appear in menu)
- [ ] Test FuelTank rendering with fluids
- [ ] Test handcar placement and synchronization
- [ ] Test train removal with JourneyMap integration enabled
- [ ] Check console for mixin warnings/errors

### Build Requirements

- Minecraft 1.21.1
- NeoForge 21.1.72+
- Create 6.0.7-117 for NeoForge 1.21.1
- Catnip 0.8.54
- Network access to maven.neoforged.net and maven.createmod.net

## Further Work

If mixins fail at runtime:

1. **Lambda method name changed**:
   - Use MixinExtras or consider a different injection strategy
   - May need to inspect Create's compiled code to find new lambda names

2. **Ordinals changed**:
   - Inspect Create's TrainPacket.write() method return points
   - Adjust ordinals or switch to different injection points

3. **Pack registration issues**:
   - Verify resource pack paths exist in mod resources
   - Check NeoForge logs for pack loading errors
   - May need to adjust PackSelectionConfig parameters

## References

- NeoForge 1.21 Migration Guide: https://docs.neoforged.net/docs/1.21.x/migration/
- Catnip Documentation: https://github.com/Create-Mod/catnip
- Create for NeoForge 1.21.1: https://maven.createmod.net/com/simibubi/create/create-1.21.1/
