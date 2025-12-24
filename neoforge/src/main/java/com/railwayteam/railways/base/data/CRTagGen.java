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

package com.railwayteam.railways.base.data;

import com.railwayteam.railways.multiloader.CommonTags;
import com.railwayteam.railways.registry.CRTags;
import com.railwayteam.railways.registry.CRTags.AllBlockTags;
import com.railwayteam.railways.registry.CRTags.AllItemTags;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.data.TagGen;
import com.tterrag.registrate.providers.RegistrateTagsProvider;
import net.minecraft.data.tags.TagsProvider.TagAppender;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Based on {@link TagGen}
 */
public class CRTagGen {
	private static final Map<TagKey<Block>, List<ResourceLocation>> OPTIONAL_TAGS = new HashMap<>();

	@SafeVarargs
	public static void addOptionalTag(ResourceLocation id, TagKey<Block>... tags) {
		for (TagKey<Block> tag : tags) {
			OPTIONAL_TAGS.computeIfAbsent(tag, (e) -> new ArrayList<>()).add(id);
		}
	}

	public static void generateBlockTags(RegistrateTagsProvider<Block> prov) {
	prov.addTag(CRTags.AllBlockTags.SEMAPHORE_POLES.tag)
		.add(AllBlocks.METAL_GIRDER.get().builtInRegistryHolder().key(), AllBlocks.METAL_GIRDER_ENCASED_SHAFT.get().builtInRegistryHolder().key())
		.addTag(BlockTags.FENCES);

		prov.addTag(CRTags.AllBlockTags.TRACK_CASING_BLACKLIST.tag);

		// VALIDATE

		for (CRTags.AllBlockTags tag : CRTags.AllBlockTags.values()) {
			if (tag.alwaysDatagen) {
				tagAppender(prov, tag);
			}
		}
		for (TagKey<Block> tag : OPTIONAL_TAGS.keySet()) {
			var appender = tagAppender(prov, tag);
			for (ResourceLocation loc : OPTIONAL_TAGS.get(tag))
				appender.addOptional(loc);
		}
	}

	public static void generateItemTags(RegistrateTagsProvider<Item> prov) {
		// Generate internal tags with concrete items (NeoForge only, no multi-loader fallbacks)
		CommonTags.DYES.forEach((color, tag) -> tagAppender(prov, tag.tag)
				.add(getDyeItem(color).builtInRegistryHolder().key()));
		
		tagAppender(prov, CommonTags.IRON_NUGGETS.tag)
				.add(Items.IRON_NUGGET.builtInRegistryHolder().key());
		tagAppender(prov, CommonTags.ZINC_NUGGETS.tag)
				.add(AllItems.ZINC_NUGGET.get().builtInRegistryHolder().key());
		tagAppender(prov, CommonTags.BRASS_NUGGETS.tag)
				.add(AllItems.BRASS_NUGGET.get().builtInRegistryHolder().key());
		
		tagAppender(prov, CommonTags.COPPER_INGOTS.tag)
				.add(Items.COPPER_INGOT.builtInRegistryHolder().key());
		tagAppender(prov, CommonTags.BRASS_INGOTS.tag)
				.add(AllItems.BRASS_INGOT.get().builtInRegistryHolder().key());
		tagAppender(prov, CommonTags.IRON_INGOTS.tag)
				.add(Items.IRON_INGOT.builtInRegistryHolder().key());
		
		tagAppender(prov, CommonTags.STRING.tag)
				.add(Items.STRING.builtInRegistryHolder().key());
		
		tagAppender(prov, CommonTags.IRON_PLATES.tag)
				.add(AllItems.IRON_SHEET.get().builtInRegistryHolder().key());
		tagAppender(prov, CommonTags.BRASS_PLATES.tag)
				.add(AllItems.BRASS_SHEET.get().builtInRegistryHolder().key());
		
		tagAppender(prov, CommonTags.WORKBENCH.tag)
				.add(Items.CRAFTING_TABLE.builtInRegistryHolder().key());

		prov.addTag(AllItemTags.NOT_TRAIN_FUEL.tag);

		for (AllItemTags tag : AllItemTags.values()) {
			if (tag.alwaysDatagen)
				tagAppender(prov, tag);
		}
	}

	private static Item getDyeItem(net.minecraft.world.item.DyeColor color) {
		return switch (color) {
			case BLACK -> Items.BLACK_DYE;
			case BLUE -> Items.BLUE_DYE;
			case BROWN -> Items.BROWN_DYE;
			case CYAN -> Items.CYAN_DYE;
			case GRAY -> Items.GRAY_DYE;
			case GREEN -> Items.GREEN_DYE;
			case LIGHT_BLUE -> Items.LIGHT_BLUE_DYE;
			case LIGHT_GRAY -> Items.LIGHT_GRAY_DYE;
			case LIME -> Items.LIME_DYE;
			case MAGENTA -> Items.MAGENTA_DYE;
			case ORANGE -> Items.ORANGE_DYE;
			case PINK -> Items.PINK_DYE;
			case PURPLE -> Items.PURPLE_DYE;
			case RED -> Items.RED_DYE;
			case WHITE -> Items.WHITE_DYE;
			case YELLOW -> Items.YELLOW_DYE;
		};
	}

	public static TagAppender<Item> tagAppender(RegistrateTagsProvider<Item> prov, AllItemTags tag) {
		return tagAppender(prov, tag.tag);
	}

	public static TagAppender<Block> tagAppender(RegistrateTagsProvider<Block> prov, AllBlockTags tag) {
		return tagAppender(prov, tag.tag);
	}

	public static <T> TagAppender<T> tagAppender(RegistrateTagsProvider<T> prov, TagKey<T> tag) {
		return com.railwayteam.railways.base.data.neoforge.CRTagGenImpl.tagAppender(prov, tag);
	}
}
