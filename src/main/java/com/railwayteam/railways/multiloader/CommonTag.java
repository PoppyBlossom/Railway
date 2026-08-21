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

package com.railwayteam.railways.multiloader;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

/**
 * A common tag wrapping a single {@code c:<forge>} conventional tag.
 * All three fields ({@link #tag}, {@link #fabric}, {@link #forge}) point to the
 * same tag key. NeoForge 1.21.1 only.
 */
public class CommonTag<T> {
	public final TagKey<T> tag, fabric, forge;

	public CommonTag(TagKey<T> common, TagKey<T> fabric, TagKey<T> forge) {
		this.tag = common;
		this.fabric = fabric;
		this.forge = forge;
	}

	public CommonTag(ResourceKey<? extends Registry<T>> registry, ResourceLocation common, ResourceLocation fabric, ResourceLocation forge) {
		this(TagKey.create(registry, common), TagKey.create(registry, fabric), TagKey.create(registry, forge));
	}

	public static <T> CommonTag<T> conventional(ResourceKey<? extends Registry<T>> registry, String common, String fabric, String forge) {
		ResourceLocation commonId = ResourceLocation.fromNamespaceAndPath("c", forge);
		TagKey<T> commonTag = TagKey.create(registry, commonId);
		return new CommonTag<>(commonTag, commonTag, commonTag);
	}
}
