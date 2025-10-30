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

package com.railwayteam.railways.content.conductor;

import com.railwayteam.railways.Railways;
import com.simibubi.create.AllBlocks;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;

public abstract class ConductorCapItem extends ArmorItem {
  public final DyeColor color;
  public final ResourceLocation textureId;
  public final String textureStr;
  
  // Create the armor material holder
  private static final Holder<ArmorMaterial> CONDUCTOR_CAP_MATERIAL = createConductorCapMaterial();

  protected ConductorCapItem(Properties props, DyeColor color) {
    super(CONDUCTOR_CAP_MATERIAL, Type.HELMET, props);
    this.color  = color;
    String colorName = color.getName().toLowerCase(Locale.ROOT);
    this.textureId = Railways.asResource("textures/entity/caps/%s_conductor_cap.png".formatted(colorName));
    this.textureStr = textureId.toString();
  }
  
  private static Holder<ArmorMaterial> createConductorCapMaterial() {
    // Create the ArmorMaterial instance  
    ArmorMaterial material = new ArmorMaterial(
      Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
        map.put(ArmorItem.Type.BOOTS, 0);
        map.put(ArmorItem.Type.LEGGINGS, 0);
        map.put(ArmorItem.Type.CHESTPLATE, 0);
        map.put(ArmorItem.Type.HELMET, 0);
        map.put(ArmorItem.Type.BODY, 0);
      }),
      0, // enchantmentValue
      SoundEvents.ARMOR_EQUIP_LEATHER,
      () -> Ingredient.EMPTY, // repairIngredient
      List.of(new ArmorMaterial.Layer(Railways.asResource("conductor_cap"))), // layers
      0f, // toughness
      0f  // knockbackResistance
    );
    // Wrap in a direct holder for 1.21; avoid mutating registries at runtime
    return Holder.direct(material);
  }  public static ConductorCapItem create(Properties props, DyeColor color) {
    return com.railwayteam.railways.content.conductor.neoforge.ConductorCapItemImpl.create(props, color);
  }

  static boolean isCasing (Block block) { return block.equals( AllBlocks.ANDESITE_CASING.get()); }
  static boolean isCasing (BlockState state) { return isCasing(state.getBlock()); }
  static boolean isCasing (Level level, BlockPos pos) { return isCasing(level.getBlockState(pos)); }

  @Nonnull
  @Override
  public InteractionResult useOn (UseOnContext ctx) {
    Level level  = ctx.getLevel();
    BlockPos pos = ctx.getClickedPos();
    if (isCasing(level, pos)) {
      if (level.isClientSide)
        return InteractionResult.SUCCESS;
      level.removeBlock(pos, false);
      ConductorEntity.spawn(level, pos, ctx.getItemInHand().copy());
      if (ctx.getPlayer() != null && !ctx.getPlayer().isCreative()) {
        ctx.getItemInHand().shrink(1);
        return InteractionResult.CONSUME;
      }
      return InteractionResult.SUCCESS;
    }
    return super.useOn(ctx);
  }
}
