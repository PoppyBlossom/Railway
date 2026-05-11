package com.railwayteam.railways.neoforge;

import com.railwayteam.railways.Railways;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.AddPackFindersEvent;

public class RailwaysDataPackImpl {
	private static final String PACK_ID = "phantom_track_override";
	private static final String PACK_NAME = "Steam 'n' Rails Phantom Track Override";

	public static void onBuiltinPackRegistration(AddPackFindersEvent event) {
		if (event.getPackType() != PackType.SERVER_DATA)
			return;

		var modFile = ModList.get().getModFileById(Railways.MOD_ID);
		if (modFile == null) {
			Railways.LOGGER.error("Could not find mod file for " + Railways.MOD_ID);
			return;
		}

		var resourcePath = modFile.getFile().findResource("datapacks/" + PACK_ID);

		event.addRepositorySource(consumer -> {
			PackLocationInfo packInfo = new PackLocationInfo(
				Railways.asResource(PACK_ID).toString(),
				Component.literal(PACK_NAME),
				PackSource.BUILT_IN,
				java.util.Optional.empty()
			);

			PackSelectionConfig selectionConfig = new PackSelectionConfig(
				false,
				Pack.Position.TOP,
				false
			);

			Pack newPack = Pack.readMetaAndCreate(
				packInfo,
				new Pack.ResourcesSupplier() {
					@Override
					public PathPackResources openPrimary(PackLocationInfo info) {
						return new PathPackResources(info, resourcePath);
					}

					@Override
					public PathPackResources openFull(PackLocationInfo info, Pack.Metadata metadata) {
						return new PathPackResources(info, resourcePath);
					}
				},
				PackType.SERVER_DATA,
				selectionConfig
			);

			if (newPack != null)
				consumer.accept(newPack);
		});
	}
}
