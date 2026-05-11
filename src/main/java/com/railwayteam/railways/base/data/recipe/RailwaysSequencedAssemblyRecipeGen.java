package com.railwayteam.railways.base.data.recipe;

import com.railwayteam.railways.Railways;
import com.railwayteam.railways.multiloader.CommonTags;
import com.railwayteam.railways.registry.CRItems;
import com.railwayteam.railways.registry.CRTrackMaterials;
import com.railwayteam.railways.util.TextUtils;
import com.simibubi.create.AllTags;
import com.simibubi.create.content.kinetics.deployer.DeployerApplicationRecipe;
import com.simibubi.create.content.kinetics.press.PressingRecipe;
import com.simibubi.create.content.kinetics.saw.CuttingRecipe;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipeBuilder;
import com.simibubi.create.content.trains.track.TrackMaterial;
import com.simibubi.create.AllItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.AllBlocks;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.Registries;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static com.railwayteam.railways.compat.tracks.TrackCompatUtils.TRACK_COMPAT_MODS;

public class RailwaysSequencedAssemblyRecipeGen extends RailwaysRecipeProvider {

    public RailwaysSequencedAssemblyRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider);
    }

    public static RailwaysSequencedAssemblyRecipeGen create(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        return new RailwaysSequencedAssemblyRecipeGen(output, lookupProvider);
    }

    protected GeneratedRecipe create(String name, Function<RailwaysSequencedAssemblyRecipeBuilder, SequencedAssemblyRecipeBuilder> transform) {
        GeneratedRecipe generatedRecipe =
            c -> transform.apply(new RailwaysSequencedAssemblyRecipeBuilder(Railways.asResource(name)))
                .build(c);
        all.add(generatedRecipe);
        return generatedRecipe;
    }

    private static Ingredient resolveCompatSleeperIngredient(TrackMaterial material) {
        if (!material.sleeperIngredient.isEmpty()) {
            return material.sleeperIngredient;
        }
        ResourceLocation tagId = ResourceLocation.fromNamespaceAndPath("railways", "compat_slabs/" + material.id.getNamespace() + "/" + material.resourceName());
        return Ingredient.of(TagKey.create(Registries.ITEM, tagId));
    }

    final EnumMap<DyeColor, GeneratedRecipe> CONDUCTOR_CAPS = new EnumMap<>(DyeColor.class);
    final Map<TrackMaterial, GeneratedRecipe> TRACKS = new HashMap<>();
    {
        for (DyeColor color : DyeColor.values()) {
            String colorName = TextUtils.titleCaseConversion(color.getName().replace("_", " "));
            String colorReg  = color.getName().toLowerCase(Locale.ROOT);
            CONDUCTOR_CAPS.put(color, create(colorReg + "_conductor_cap", b -> b.require(CRItems.woolByColor(color))
                .transitionTo(CRItems.ITEM_INCOMPLETE_CONDUCTOR_CAP.get(color).get())
                .addOutput(CRItems.ITEM_CONDUCTOR_CAP.get(color).get(), 1)
                .loops(1)
                .addStep(CuttingRecipe::new, rb -> rb)
                .addStep(DeployerApplicationRecipe::new, rb -> rb.require(Ingredient.of(AllItems.PRECISION_MECHANISM)))
                .addStep(DeployerApplicationRecipe::new, rb -> rb.require(Ingredient.of(net.minecraft.world.item.Items.STRING)))
            ));
        }

        List<TrackMaterial> trackMaterials = new ArrayList<>(TrackMaterial.allFromMod(Railways.MOD_ID));

        for (String mod : TRACK_COMPAT_MODS)
            trackMaterials.addAll(TrackMaterial.allFromMod(mod));

        for (TrackMaterial material : trackMaterials) {
            boolean isCompat = TRACK_COMPAT_MODS.contains(material.id.getNamespace());
            
            if (!isCompat && (material.railsIngredient.isEmpty() || material.sleeperIngredient.isEmpty())) {
                continue;
            }

            if (material.trackType == CRTrackMaterials.CRTrackType.WIDE_GAUGE) {
                TrackMaterial baseMaterial = CRTrackMaterials.getBaseFromWide(material);
                if (baseMaterial == null) continue;
                
                Ingredient sleeperIngredient = material == CRTrackMaterials.WIDE_GAUGE_ANDESITE ? Ingredient.of(AllTags.AllItemTags.SLEEPERS.tag) : resolveCompatSleeperIngredient(baseMaterial);
                
                TRACKS.put(material, create(
                    "track_" + (material.id.getNamespace().equals(Railways.MOD_ID) ? "" : material.id.getNamespace()+"_") + material.resourceName(),
                    b -> b.conditionalMaterial(material).require(baseMaterial.getBlock())
                        .transitionTo(CRItems.ITEM_INCOMPLETE_TRACK.get(material).get())
                        .addOutput(material.getBlock(), 1)
                        .loops(1)
                        .addStep(CuttingRecipe::new, rb -> rb)
                        .addStep(DeployerApplicationRecipe::new, rb -> rb.require(sleeperIngredient))
                        .addStep(PressingRecipe::new, rb -> rb)
                ));
            } else if (material.trackType == CRTrackMaterials.CRTrackType.NARROW_GAUGE) {
                TrackMaterial baseMaterial = CRTrackMaterials.getBaseFromNarrow(material);
                if (baseMaterial == null) continue;
                
                Ingredient sleeperIngredient = material == CRTrackMaterials.NARROW_GAUGE_ANDESITE ? Ingredient.of(AllTags.AllItemTags.SLEEPERS.tag) : resolveCompatSleeperIngredient(baseMaterial);
                Ingredient finalRailsIngredient = isCompat ? baseMaterial.railsIngredient : baseMaterial.railsIngredient;

                TRACKS.put(material, create(
                    "track_" + (material.id.getNamespace().equals(Railways.MOD_ID) ? "" : material.id.getNamespace()+"_") + material.resourceName(),
                    b -> b.conditionalMaterial(material).require(sleeperIngredient)
                        .transitionTo(CRItems.ITEM_INCOMPLETE_TRACK.get(material).get())
                        .addOutput(material.getBlock(), 1)
                        .loops(1)
                        .addStep(CuttingRecipe::new, rb -> rb)
                        .addStep(DeployerApplicationRecipe::new, rb -> rb.require(finalRailsIngredient))
                        .addStep(PressingRecipe::new, rb -> rb)
                ));
            } else {
                Ingredient sleeperIngredient = resolveCompatSleeperIngredient(material);
                Ingredient finalRailsIngredient = material.railsIngredient;

                TRACKS.put(material, create(
                    "track_" + (material.id.getNamespace().equals(Railways.MOD_ID) ? "" : material.id.getNamespace()+"_") + material.resourceName(),
                    b -> b.conditionalMaterial(material).require(sleeperIngredient)
                        .transitionTo(CRItems.ITEM_INCOMPLETE_TRACK.get(material).get())
                        .addOutput(material.getBlock(), 1)
                        .loops(1)
                        .addStep(DeployerApplicationRecipe::new, rb -> rb.require(finalRailsIngredient))
                        .addStep(DeployerApplicationRecipe::new, rb -> rb.require(finalRailsIngredient))
                        .addStep(PressingRecipe::new, rb -> rb)
                ));
            }
        }

        TRACKS.put(CRTrackMaterials.PHANTOM, create("track_phantom", b -> b.require(Ingredient.of(net.minecraft.world.item.Items.PHANTOM_MEMBRANE))
            .transitionTo(CRItems.ITEM_INCOMPLETE_TRACK.get(CRTrackMaterials.PHANTOM).get())
            .addOutput(new ItemStack(CRTrackMaterials.PHANTOM.getBlock(), 32), 1)
            .loops(1)
            .addStep(DeployerApplicationRecipe::new, rb -> rb.require(Ingredient.of(CommonTags.IRON_INGOTS.tag)))
            .addStep(DeployerApplicationRecipe::new, rb -> rb.require(Ingredient.of(CommonTags.IRON_INGOTS.tag)))
            .addStep(PressingRecipe::new, rb -> rb)
        ));

        TRACKS.put(CRTrackMaterials.WIDE_GAUGE_PHANTOM, create("track_phantom_wide", b -> b.require(CRTrackMaterials.PHANTOM.getBlock())
            .transitionTo(CRItems.ITEM_INCOMPLETE_TRACK.get(CRTrackMaterials.WIDE_GAUGE_PHANTOM).get())
            .addOutput(CRTrackMaterials.WIDE_GAUGE_PHANTOM.getBlock(), 1)
            .loops(1)
            .addStep(CuttingRecipe::new, rb -> rb)
            .addStep(DeployerApplicationRecipe::new, rb -> rb.require(Ingredient.of(net.minecraft.world.item.Items.PHANTOM_MEMBRANE)))
            .addStep(PressingRecipe::new, rb -> rb)
        ));

        TRACKS.put(CRTrackMaterials.NARROW_GAUGE_PHANTOM, create("track_phantom_narrow", b -> b.require(Ingredient.of(net.minecraft.world.item.Items.PHANTOM_MEMBRANE))
            .transitionTo(CRItems.ITEM_INCOMPLETE_TRACK.get(CRTrackMaterials.NARROW_GAUGE_PHANTOM).get())
            .addOutput(new ItemStack(CRTrackMaterials.NARROW_GAUGE_PHANTOM.getBlock(), 32), 1)
            .loops(1)
            .addStep(CuttingRecipe::new, rb -> rb)
            .addStep(DeployerApplicationRecipe::new, rb -> rb.require(Ingredient.of(CommonTags.IRON_INGOTS.tag)))
            .addStep(PressingRecipe::new, rb -> rb)
        ));

        TRACKS.put(CRTrackMaterials.MONORAIL, create("track_monorail", b -> b.require(Ingredient.of(AllBlocks.METAL_GIRDER.get()))
            .transitionTo(CRItems.ITEM_INCOMPLETE_TRACK.get(CRTrackMaterials.MONORAIL).get())
            .addOutput(new ItemStack(CRTrackMaterials.MONORAIL.getBlock(), 6), 1)
            .loops(1)
            .addStep(DeployerApplicationRecipe::new, rb -> rb.require(Ingredient.of(AllBlocks.METAL_BRACKET.get())))
            .addStep(DeployerApplicationRecipe::new, rb -> rb.require(Ingredient.of(AllItems.IRON_SHEET.get())))
            .addStep(PressingRecipe::new, rb -> rb)
        ));
    }
}
