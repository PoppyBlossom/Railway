package com.railwayteam.railways.neoforge;

import com.railwayteam.railways.Railways;
import com.railwayteam.railways.registry.neoforge.CRCreativeModeTabsImpl;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModSetupImpl {
    public static void useBaseTab() {
        setTab(CRCreativeModeTabsImpl.MAIN_TAB);
    }

    public static void useTracksTab() {
        setTab(CRCreativeModeTabsImpl.TRACKS_TAB);
    }

    public static void usePalettesTab() {
        setTab(CRCreativeModeTabsImpl.PALETTES_TAB);
    }

    private static void setTab(DeferredHolder<CreativeModeTab, CreativeModeTab> tab) {
        Railways.registrate().setCreativeTab(tab);
    }
}
