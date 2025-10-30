#!/usr/bin/env python3
"""
Fix all AssertionError stubs in the multiloader pattern by calling their platform implementations.
"""

import re
from pathlib import Path

# Mapping of stub files to their implementation files (relative to neoforge/src/main/java)
STUB_TO_IMPL = {
    # Track generators
    "com/railwayteam/railways/content/custom_tracks/wide_gauge/WideGaugeTrackBlockStateGenerator.java": 
        "com/railwayteam/railways/content/custom_tracks/wide_gauge/neoforge/WideGaugeTrackBlockStateGeneratorImpl.java",
    "com/railwayteam/railways/content/custom_tracks/monorail/MonorailBlockStateGenerator.java": 
        "com/railwayteam/railways/content/custom_tracks/monorail/neoforge/MonorailBlockStateGeneratorImpl.java",
    "com/railwayteam/railways/content/custom_tracks/narrow_gauge/NarrowGaugeTrackBlockStateGenerator.java": 
        "com/railwayteam/railways/content/custom_tracks/narrow_gauge/neoforge/NarrowGaugeTrackBlockStateGeneratorImpl.java",
    "com/railwayteam/railways/compat/tracks/WideGaugeCompatTrackBlockStateGenerator.java":
        "com/railwayteam/railways/compat/tracks/neoforge/WideGaugeCompatTrackBlockStateGeneratorImpl.java",
    "com/railwayteam/railways/compat/tracks/NarrowGaugeCompatTrackBlockStateGenerator.java":
        "com/railwayteam/railways/compat/tracks/neoforge/NarrowGaugeCompatTrackBlockStateGeneratorImpl.java",
    "com/railwayteam/railways/compat/tracks/CompatTrackBlockStateGenerator.java":
        "com/railwayteam/railways/compat/tracks/neoforge/CompatTrackBlockStateGeneratorImpl.java",
    
    # Multiloader utils
    "com/railwayteam/railways/multiloader/Env.java":
        "com/railwayteam/railways/multiloader/neoforge/EnvImpl.java",
    "com/railwayteam/railways/multiloader/PlayerSelection.java":
        "com/railwayteam/railways/multiloader/neoforge/PlayerSelectionImpl.java",
    "com/railwayteam/railways/multiloader/PlatformAbstractionHelper.java":
        "com/railwayteam/railways/multiloader/neoforge/PlatformAbstractionHelperImpl.java",
    "com/railwayteam/railways/multiloader/PacketSet.java":
        "com/railwayteam/railways/multiloader/neoforge/PacketSetImpl.java",
    "com/railwayteam/railways/multiloader/EntityTypeConfigurator.java":
        "com/railwayteam/railways/multiloader/neoforge/EntityTypeConfiguratorImpl.java",
    "com/railwayteam/railways/multiloader/ClientCommands.java":
        "com/railwayteam/railways/multiloader/neoforge/ClientCommandsImpl.java",
    
    # Utils
    "com/railwayteam/railways/util/AbstractionUtils.java":
        "com/railwayteam/railways/util/neoforge/AbstractionUtilsImpl.java",
    "com/railwayteam/railways/util/BlockStateUtils.java":
        "com/railwayteam/railways/util/neoforge/BlockStateUtilsImpl.java",
    "com/railwayteam/railways/util/EntityUtils.java":
        "com/railwayteam/railways/util/neoforge/EntityUtilsImpl.java",
    "com/railwayteam/railways/util/FluidUtils.java":
        "com/railwayteam/railways/util/neoforge/FluidUtilsImpl.java",
    "com/railwayteam/railways/util/ItemUtils.java":
        "com/railwayteam/railways/util/neoforge/ItemUtilsImpl.java",
    "com/railwayteam/railways/util/RegistrationListening.java":
        "com/railwayteam/railways/util/neoforge/RegistrationListeningImpl.java",
    "com/railwayteam/railways/util/client/ClientUtils.java":
        "com/railwayteam/railways/util/client/neoforge/ClientUtilsImpl.java",
    
    # Registry
    "com/railwayteam/railways/registry/CRKeys.java":
        "com/railwayteam/railways/registry/neoforge/CRKeysImpl.java",
    "com/railwayteam/railways/registry/CRParticleTypes.java":
        "com/railwayteam/railways/registry/neoforge/CRParticleTypesParticleEntryImpl.java",
    "com/railwayteam/railways/registry/CRExtraRegistration.java":
        "com/railwayteam/railways/registry/neoforge/CRExtraRegistrationImpl.java",
    
    # Compat
    "com/railwayteam/railways/compat/Mods.java":
        "com/railwayteam/railways/compat/neoforge/ModsImpl.java",
    
    # Content
    "com/railwayteam/railways/content/conductor/vent/VentBlock.java":
        "com/railwayteam/railways/content/conductor/vent/neoforge/VentBlockImpl.java",
    "com/railwayteam/railways/content/conductor/toolbox/MountedToolboxDisposeAllPacket.java":
        "com/railwayteam/railways/content/conductor/toolbox/neoforge/MountedToolboxDisposeAllPacketImpl.java",
    "com/railwayteam/railways/content/conductor/toolbox/MountedToolboxEquipPacket.java":
        "com/railwayteam/railways/content/conductor/toolbox/neoforge/MountedToolboxEquipPacketImpl.java",
    "com/railwayteam/railways/content/switches/TrackSwitchBlock.java":
        "com/railwayteam/railways/content/switches/neoforge/TrackSwitchBlockImpl.java",
    "com/railwayteam/railways/content/fuel/LiquidFuelTrainHandler.java":
        "com/railwayteam/railways/content/fuel/neoforge/LiquidFuelTrainHandlerImpl.java",
    "com/railwayteam/railways/content/roller_extensions/TrackReplacePaver.java":
        "com/railwayteam/railways/content/roller_extensions/neoforge/TrackReplacePaverImpl.java",
    "com/railwayteam/railways/content/conductor/ConductorCapItem.java":
        "com/railwayteam/railways/content/conductor/neoforge/ConductorCapItemImpl.java",
    "com/railwayteam/railways/content/palettes/boiler/BoilerGenerator.java":
        "com/railwayteam/railways/content/palettes/boiler/neoforge/BoilerGeneratorImpl.java",
    "com/railwayteam/railways/content/palettes/boiler/BoilerBlock.java":
        "com/railwayteam/railways/content/palettes/boiler/neoforge/BoilerBlockPlacementHelperImpl.java",
    "com/railwayteam/railways/content/custom_tracks/casing/CasingCollisionBlock.java":
        "com/railwayteam/railways/content/custom_tracks/casing/neoforge/CasingCollisionBlockImpl.java",
    "com/railwayteam/railways/content/coupling/coupler/TrackCouplerBlock.java":
        "com/railwayteam/railways/content/coupling/coupler/neoforge/TrackCouplerBlockImpl.java",
    
    # Client
    "com/railwayteam/railways/RailwaysClient.java":
        "com/railwayteam/railways/neoforge/RailwaysClientImpl.java",
    "com/railwayteam/railways/content/conductor/vent/CopycatVentModel.java":
        "com/railwayteam/railways/content/conductor/vent/neoforge/CopycatVentModelImpl.java",
}

ROOT = Path(__file__).parent / "neoforge" / "src" / "main" / "java"


def get_impl_class(stub_path):
    """Convert stub path to impl class name."""
    stub_rel = stub_path.replace(".java", "")
    if "/neoforge/" in stub_rel:
        # Already in neoforge package
        return stub_rel.replace("/", ".") + "Impl"
    else:
        # Add neoforge subpackage before class name
        parts = stub_rel.split("/")
        parts.insert(-1, "neoforge")
        return ".".join(parts) + "Impl"


def fix_static_method(content, class_name):
    """Fix throw new AssertionError() in static methods."""
    impl_class = get_impl_class(class_name)
    
    # Pattern: static method that throws AssertionError
    # Breakdown of the regex pattern:
    # (public\s+static\s+         # Match 'public static' modifiers
    #   (?:<[^>]+>\s+)?           # Optional generics, e.g. <T>
    #   (?:\w+(?:<[^>]+>)?)       # Return type, possibly with generics
    #   \s+(\w+)                  # Method name (captured group 2)
    #   \([^)]*\)                 # Parameter list (anything inside parentheses)
    #   \s*\{)                    # Opening brace of method body (captured group 1)
    # \s*throw\s+new\s+AssertionError\(\); # The body: throw new AssertionError();
    pattern = r'(public\s+static\s+(?:<[^>]+>\s+)?(?:\w+(?:<[^>]+>)?)\s+(\w+)\([^)]*\)\s*\{)\s*throw\s+new\s+AssertionError\(\);'
    
    def replace(match):
        signature = match.group(1)
        method_name = match.group(2)
        # Get parameters from signature
        params_match = re.search(r'\(([^)]*)\)', signature)
        params = params_match.group(1) if params_match else ""
        
        # Extract parameter names (ignoring types)
        param_names = []
        if params.strip():
            for param in params.split(","):
                param = param.strip()
                if param:
                    # Get last word (parameter name)
                    parts = param.split()
                    if parts:
                        param_names.append(parts[-1])
        
        param_call = ", ".join(param_names)
        # Extract indentation from the method signature line
        indent_match = re.match(r"([ \t]*)", signature)
        indent = indent_match.group(1) if indent_match else ""
        return f"{signature}\n{indent}    return {impl_class}.{method_name}({param_call});"
    
    return re.sub(pattern, replace, content)


def process_file(stub_file):
    """Process a single stub file."""
    stub_path = ROOT / stub_file
    if not stub_path.exists():
        print(f"⚠️  Skip (not found): {stub_file}")
        return False
    
    content = stub_path.read_text(encoding='utf-8')
    original = content
    
    # Fix static methods
    content = fix_static_method(content, stub_file)
    
    if content != original:
        stub_path.write_text(content, encoding='utf-8')
        print(f"✅ Fixed: {stub_file}")
        return True
    else:
        print(f"  No changes: {stub_file}")
        return False


def main():
    print("Fixing multiloader stubs...")
    print("=" * 60)
    
    fixed_count = 0
    for stub_file in STUB_TO_IMPL.keys():
        if process_file(stub_file):
            fixed_count += 1
    
    print("=" * 60)
    print(f"✅ Fixed {fixed_count} files")


if __name__ == "__main__":
    main()
