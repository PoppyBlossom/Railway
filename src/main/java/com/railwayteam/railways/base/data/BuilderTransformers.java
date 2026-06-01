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

import com.railwayteam.railways.content.buffer.MonoTrackBufferBlock;
import com.railwayteam.railways.content.buffer.TrackBufferBlock;
import com.railwayteam.railways.content.buffer.headstock.CopycatHeadstockBarsBlock;
import com.railwayteam.railways.content.buffer.headstock.CopycatHeadstockBlock;
import com.railwayteam.railways.content.buffer.headstock.HeadstockBlock;
import com.railwayteam.railways.content.buffer.single_deco.GenericDyeableSingleBufferBlock;
import com.railwayteam.railways.content.buffer.single_deco.LinkPinBlock;
import com.railwayteam.railways.content.conductor.vent.VentBlock;
import com.railwayteam.railways.content.conductor.whistle.ConductorWhistleFlagBlock;
import com.railwayteam.railways.content.coupling.coupler.TrackCouplerBlock;
import com.railwayteam.railways.content.custom_bogeys.blocks.base.CRBogeyBlock;
import com.railwayteam.railways.content.custom_tracks.casing.CasingCollisionBlock;
import com.railwayteam.railways.content.custom_bogeys.special.invisible.InvisibleBogeyBlock;
import com.railwayteam.railways.content.custom_bogeys.special.monobogey.InvisibleMonoBogeyBlock;
import com.railwayteam.railways.content.custom_bogeys.special.monobogey.MonoBogeyBlock;
import com.railwayteam.railways.content.custom_tracks.generic_crossing.GenericCrossingBlock;
import com.railwayteam.railways.content.handcar.HandcarBlock;
import com.railwayteam.railways.content.palettes.boiler.BoilerBlock;
import com.railwayteam.railways.content.palettes.boiler.BoilerGenerator;
import com.railwayteam.railways.content.palettes.smokebox.PalettesSmokeboxBlock;
import com.railwayteam.railways.content.semaphore.SemaphoreBlock;
import com.railwayteam.railways.content.smokestack.block.DieselSmokeStackBlock;
import com.railwayteam.railways.content.smokestack.block.SmokeStackBlock;
import com.railwayteam.railways.content.switches.TrackSwitchBlock;
import com.railwayteam.railways.registry.CRPalettes.Wrapping;
import com.railwayteam.railways.registry.CRTags;
import com.railwayteam.railways.util.ColorUtils;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllTags;
import com.simibubi.create.foundation.data.SharedProperties;
import com.simibubi.create.foundation.item.ItemDescription;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.function.Function;

import static com.simibubi.create.foundation.data.TagGen.pickaxeOnly;

public class BuilderTransformers {    public static <B extends MonoBogeyBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> monobogey() {
        return com.railwayteam.railways.base.data.neoforge.BuilderTransformersImpl.monobogey();
    }    public static <B extends InvisibleBogeyBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> invisibleBogey() {
        return com.railwayteam.railways.base.data.neoforge.BuilderTransformersImpl.invisibleBogey();
    }    public static <B extends InvisibleMonoBogeyBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> invisibleMonoBogey() {
        return com.railwayteam.railways.base.data.neoforge.BuilderTransformersImpl.invisibleMonoBogey();
    }

    @ApiStatus.Internal
    public static <B extends CRBogeyBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> sharedBogey() {
        return b -> b.initialProperties(SharedProperties::softMetal)
            .properties(p -> p.sound(SoundType.NETHERITE_BLOCK))
            .properties(BlockBehaviour.Properties::noOcclusion)
            .transform(pickaxeOnly())
            .tag(AllTags.AllBlockTags.SAFE_NBT.tag)
            .loot((p, l) -> p.dropOther(l, AllBlocks.RAILWAY_CASING.get()));
    }    public static <B extends CRBogeyBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> standardBogey() {
        return com.railwayteam.railways.base.data.neoforge.BuilderTransformersImpl.standardBogey();
    }    public static <B extends CRBogeyBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> wideBogey() {
        return com.railwayteam.railways.base.data.neoforge.BuilderTransformersImpl.wideBogey();
    }    public static <B extends CRBogeyBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> narrowBogey() {
        return com.railwayteam.railways.base.data.neoforge.BuilderTransformersImpl.narrowBogey();
    }    public static <B extends SmokeStackBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> smokestack(boolean rotates, ResourceLocation modelLoc) {
        return com.railwayteam.railways.base.data.neoforge.BuilderTransformersImpl.smokestack(rotates, modelLoc);
    }    public static <B extends SemaphoreBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> semaphore() {
        return com.railwayteam.railways.base.data.neoforge.BuilderTransformersImpl.semaphore();
    }    public static <B extends TrackCouplerBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> trackCoupler() {
        return com.railwayteam.railways.base.data.neoforge.BuilderTransformersImpl.trackCoupler();
    }    public static <B extends TrackSwitchBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> trackSwitch(boolean andesite) {
        return com.railwayteam.railways.base.data.neoforge.BuilderTransformersImpl.trackSwitch(andesite);
    }    public static <B extends ConductorWhistleFlagBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> conductorWhistleFlag() {
        return com.railwayteam.railways.base.data.neoforge.BuilderTransformersImpl.conductorWhistleFlag();
    }    public static <B extends DieselSmokeStackBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> dieselSmokeStack() {
        return com.railwayteam.railways.base.data.neoforge.BuilderTransformersImpl.dieselSmokeStack();
    }    public static <B extends VentBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> conductorVent() {
        return com.railwayteam.railways.base.data.neoforge.BuilderTransformersImpl.conductorVent();
    }    public static NonNullBiConsumer<DataGenContext<Block, SmokeStackBlock>, RegistrateBlockstateProvider> defaultSmokeStack(String variant, SmokeStackBlock.RotationType rotType) {
        return com.railwayteam.railways.base.data.neoforge.BuilderTransformersImpl.defaultSmokeStack(variant, rotType);
    }    public static <B extends CasingCollisionBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> casingCollision() {
        return com.railwayteam.railways.base.data.neoforge.BuilderTransformersImpl.casingCollision();
    }    public static <B extends HandcarBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> handcar() {
        return com.railwayteam.railways.base.data.neoforge.BuilderTransformersImpl.handcar();
    }    public static <B extends GenericCrossingBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> genericCrossing() {
        return com.railwayteam.railways.base.data.neoforge.BuilderTransformersImpl.genericCrossing();
    }    public static <B extends Block, P> NonNullUnaryOperator<BlockBuilder<B, P>> locoMetalBase(@Nullable DyeColor color, @Nullable String type) {
        return com.railwayteam.railways.base.data.neoforge.BuilderTransformersImpl.locoMetalBase(color, type);
    }    public static <B extends RotatedPillarBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> locoMetalPillar(@Nullable DyeColor color) {
        return com.railwayteam.railways.base.data.neoforge.BuilderTransformersImpl.locoMetalPillar(color);
    }    public static <B extends PalettesSmokeboxBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> locoMetalSmokeBox(@Nullable DyeColor color) {
        return com.railwayteam.railways.base.data.neoforge.BuilderTransformersImpl.locoMetalSmokeBox(color);
    }

    public static <B extends BoilerBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> locoMetalBoiler(@Nullable DyeColor color, @Nullable Wrapping wrapping) {
        return b -> b.initialProperties(SharedProperties::softMetal)
            .properties(p -> p
                .mapColor(ColorUtils.mapColorFromDye(color, MapColor.COLOR_BLACK))
                .sound(SoundType.NETHERITE_BLOCK)
                .noOcclusion()
            )
            .tag(CRTags.AllBlockTags.LOCOMETAL.tag)
            .tag(CRTags.AllBlockTags.LOCOMETAL_BOILERS.tag)
            .tag(AllTags.AllBlockTags.COPYCAT_DENY.tag)
            .transform(pickaxeOnly())
            .onRegisterAfter(Registries.ITEM, v -> ItemDescription.useKey(v, "block.railways.boiler"))
            .blockstate(BoilerGenerator.create(color, wrapping)::generate);
    }

    public static String colorNameUnderscore(@Nullable DyeColor color) {
        return color == null ? "" : color.name().toLowerCase(Locale.ROOT) + "_";
    }    public static <B extends Block, P> NonNullUnaryOperator<BlockBuilder<B, P>> variantBuffer() {
        return com.railwayteam.railways.base.data.neoforge.BuilderTransformersImpl.variantBuffer();
    }    public static <I extends Item, P> NonNullUnaryOperator<ItemBuilder<I, P>> variantBufferItem() {
        return com.railwayteam.railways.base.data.neoforge.BuilderTransformersImpl.variantBufferItem();
    }    public static <B extends CopycatHeadstockBarsBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> copycatHeadstockBars() {
        return com.railwayteam.railways.base.data.neoforge.BuilderTransformersImpl.copycatHeadstockBars();
    }    public static <B extends TrackBufferBlock<?>, P> NonNullUnaryOperator<BlockBuilder<B, P>> bufferBlockState(Function<BlockState, ResourceLocation> modelFunc, Function<BlockState, Direction> facingFunc) {
        return com.railwayteam.railways.base.data.neoforge.BuilderTransformersImpl.bufferBlockState(modelFunc, facingFunc);
    }    public static <B extends MonoTrackBufferBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> monoBuffer() {
        return com.railwayteam.railways.base.data.neoforge.BuilderTransformersImpl.monoBuffer();
    }    public static <B extends LinkPinBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> linkAndPin() {
        return com.railwayteam.railways.base.data.neoforge.BuilderTransformersImpl.linkAndPin();
    }    public static <B extends HeadstockBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> headstock() {
        return com.railwayteam.railways.base.data.neoforge.BuilderTransformersImpl.headstock();
    }    public static <B extends Block, P> NonNullUnaryOperator<BlockBuilder<B, P>> invisibleBlockState() {
        return com.railwayteam.railways.base.data.neoforge.BuilderTransformersImpl.invisibleBlockState();
    }    public static <B extends CopycatHeadstockBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> copycatHeadstock() {
        return com.railwayteam.railways.base.data.neoforge.BuilderTransformersImpl.copycatHeadstock();
    }    public static <I extends Item, P> NonNullUnaryOperator<ItemBuilder<I, P>> copycatHeadstockItem() {
        return com.railwayteam.railways.base.data.neoforge.BuilderTransformersImpl.copycatHeadstockItem();
    }    public static <B extends GenericDyeableSingleBufferBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> bigBuffer() {
        return com.railwayteam.railways.base.data.neoforge.BuilderTransformersImpl.bigBuffer();
    }    public static <B extends GenericDyeableSingleBufferBlock, P> NonNullUnaryOperator<BlockBuilder<B, P>> smallBuffer() {
        return com.railwayteam.railways.base.data.neoforge.BuilderTransformersImpl.smallBuffer();
    }
}
