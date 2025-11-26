package com.railwayteam.railways.registry.neoforge;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.registries.DeferredHolder;

public class CRCreativeModeTabsRegistrateDisplayItemsGeneratorImpl {
    public static boolean isInCreativeTab(RegistryEntry<?, ?> entry, ResourceKey<CreativeModeTab> tab) {
        DeferredHolder<CreativeModeTab, CreativeModeTab> holder = resolveHolder(tab);
        if (holder == null)
            return true;
        return CreateRegistrate.isInCreativeTab(entry, holder);
    }

    private static DeferredHolder<CreativeModeTab, CreativeModeTab> resolveHolder(ResourceKey<CreativeModeTab> tab) {
        if (CRCreativeModeTabsImpl.MAIN_TAB.getKey().equals(tab))
            return CRCreativeModeTabsImpl.MAIN_TAB;
        if (CRCreativeModeTabsImpl.TRACKS_TAB.getKey().equals(tab))
            return CRCreativeModeTabsImpl.TRACKS_TAB;
        if (CRCreativeModeTabsImpl.PALETTES_TAB.getKey().equals(tab))
            return CRCreativeModeTabsImpl.PALETTES_TAB;
        return null;
    }
}
