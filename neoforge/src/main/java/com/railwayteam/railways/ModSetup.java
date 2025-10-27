/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2024 The Railways Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.railwayteam.railways;

import com.railwayteam.railways.compat.tracks.mods.*;
import com.railwayteam.railways.content.custom_tracks.casing.CasingCollisionUtils;
import com.railwayteam.railways.registry.*;
import com.railwayteam.railways.neoforge.RailwaysImpl;
import com.railwayteam.railways.neoforge.ModSetupImpl;

public class ModSetup {
  // Single-loader NeoForge: delegate to the NeoForge implementation to wire Registrate creative tabs
  public static void useBaseTab() { ModSetupImpl.useBaseTab(); }
  public static void useTracksTab() { ModSetupImpl.useTracksTab(); }
  public static void usePalettesTab() { ModSetupImpl.usePalettesTab(); }

  public static void register() {
    useBaseTab();
    CRTrackMaterials.register();
    CRBogeyStyles.register();
    CRCreativeModeTabs.register();
    CRItems.register();
    CRSpriteShifts.register();
    CRDisplaySources.register();
    CRDisplayTargets.register();
    CRBlockEntities.register();

    // Switch to the Tracks tab before registering track blocks so they appear in the Tracks tab
    useTracksTab();
    CRBlocks.register();
  // Platform-specific registrations (NeoForge)
  RailwaysImpl.platformBasedRegistration();

    // Switch to the Palettes tab before registering palette items so they appear in the Palettes tab
    usePalettesTab();
    CRPalettes.register();
    CRContainerTypes.register();
    CREntities.register();
    CRSounds.register();
    CRTags.register();
    CREdgePointTypes.register();
    CRSchedule.register();
    CRDataFixers.register();
    CRExtraRegistration.platformSpecificRegistration();
    CasingCollisionUtils.register();
    CRInteractionBehaviours.register();
    CRPortalTracks.register();

  // Compat (tracks) - ensure Tracks tab is active for compat track registrations
  useTracksTab();
    HexCastingTrackCompat.register();
    BygTrackCompat.register();
    BlueSkiesTrackCompat.register();
    TwilightForestTrackCompat.register();
    BiomesOPlentyTrackCompat.register();
    NaturesSpiritTrackCompat.register();
    DreamsAndDesiresTrackCompat.register();
    QuarkTrackCompat.register();
    TFCTrackCompat.register();
  }
}
