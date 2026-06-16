/*
 * Steam 'n' Rails
 * Copyright (c) 2025 The Railways Team
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

package com.railwayteam.railways.registry;

import com.railwayteam.railways.Railways;
import com.railwayteam.railways.content.palettes.PalettesColor;
import com.railwayteam.railways.content.palettes.painting.PaintFluid;
import com.simibubi.create.content.fluids.VirtualFluid;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.FluidEntry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class CRFluids {
    private static final CreateRegistrate REGISTRATE = Railways.registrate();

    private static final ResourceLocation STILL_TEXTURE = Railways.asResource("fluid/paint_still");
    private static final ResourceLocation FLOWING_TEXTURE = Railways.asResource("fluid/paint_flow");

    public static final FluidEntry<VirtualFluid> PAINT = REGISTRATE.virtualFluid(
            "paint", STILL_TEXTURE, FLOWING_TEXTURE,
            PaintFluidType::new,
            VirtualFluid::createSource, VirtualFluid::createFlowing)
        .lang("Paint")
        .register();

    public static Fluid paintSource() {
        return PAINT.get().getSource();
    }

    public static void register() {}

    private static class PaintFluidType extends FluidType {
        private final ResourceLocation stillTexture;
        private final ResourceLocation flowingTexture;

        public PaintFluidType(Properties properties, ResourceLocation stillTexture, ResourceLocation flowingTexture) {
            super(properties);
            this.stillTexture = stillTexture;
            this.flowingTexture = flowingTexture;
        }

        @Override
        public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
            consumer.accept(new IClientFluidTypeExtensions() {
                private ResourceLocation $getStillTexture(@Nullable PalettesColor color) {
                    if (color == null) color = PalettesColor.NETHERITE;
                    return stillTexture.withSuffix("/" + color.getSerializedName());
                }

                private ResourceLocation $getFlowingTexture(@Nullable PalettesColor color) {
                    if (color == null) color = PalettesColor.NETHERITE;
                    return flowingTexture.withSuffix("/" + color.getSerializedName());
                }

                @Override
                public ResourceLocation getStillTexture() {
                    return $getStillTexture(null);
                }

                @Override
                public ResourceLocation getStillTexture(FluidStack stack) {
                    CompoundTag tag = null;
                    CustomData data = stack.get(DataComponents.CUSTOM_DATA);
                    if (data != null) tag = data.copyTag();
                    return $getStillTexture(PaintFluid.getColor(tag).orElse(null));
                }

                @Override
                public ResourceLocation getFlowingTexture() {
                    return $getFlowingTexture(null);
                }

                @Override
                public ResourceLocation getFlowingTexture(FluidStack stack) {
                    CompoundTag tag = null;
                    CustomData data = stack.get(DataComponents.CUSTOM_DATA);
                    if (data != null) tag = data.copyTag();
                    return $getFlowingTexture(PaintFluid.getColor(tag).orElse(null));
                }
            });
        }

        @Override
        public String getDescriptionId(FluidStack stack) {
            CompoundTag tag = null;
            CustomData data = stack.get(DataComponents.CUSTOM_DATA);
            if (data != null) tag = data.copyTag();
            return PaintFluid.getColor(tag)
                .map(PalettesColor::getPaintNameId)
                .orElse("fluid.railways.paint");
        }
    }
}
