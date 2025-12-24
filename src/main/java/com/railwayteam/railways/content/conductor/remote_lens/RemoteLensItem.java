/*
 * Steam 'n' Rails
 * Copyright (c) 2022-2025 The Railways Team
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

package com.railwayteam.railways.content.conductor.remote_lens;

import com.railwayteam.railways.content.conductor.ConductorEntity;
import com.railwayteam.railways.util.TextUtils;
import com.simibubi.create.AllSoundEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class RemoteLensItem extends Item {
    public RemoteLensItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        if (tag.hasUUID("SelectedConductor")) {
            UUID conductorId = tag.getUUID("SelectedConductor");

            tooltip.add(Component.translatable("railways.whistle.tool.bound").withStyle(ChatFormatting.DARK_GREEN));
            tooltip.add(TextUtils.translateWithFormatting("railways.whistle.tool.conductor_id", conductorId.toString().substring(0, 5)));
            tooltip.add(Component.translatable("railways.remote_lens.tool.bound_usage"));
        } else {
            tooltip.add(Component.translatable("railways.remote_lens.tool.not_bound").withStyle(ChatFormatting.DARK_RED));
        }
    }

    @Override
    public @NotNull InteractionResult interactLivingEntity(@NotNull ItemStack pStack, @NotNull Player pPlayer,
                                                           @NotNull LivingEntity pInteractionTarget, @NotNull InteractionHand pUsedHand) {
        if (pPlayer.level().isClientSide)
            return InteractionResult.CONSUME;
        if (pInteractionTarget instanceof ConductorEntity conductor && conductor.getJob() == ConductorEntity.Job.SPY) {
            CustomData customData = pStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
            CompoundTag stackTag = customData.copyTag();
            stackTag.putUUID("SelectedConductor", conductor.getUUID());
            pPlayer.displayClientMessage(Component.translatable("railways.remote_lens.set"), true);
            pStack.set(DataComponents.CUSTOM_DATA, CustomData.of(stackTag));
            pPlayer.setItemInHand(pUsedHand, pStack);
            AllSoundEvents.PECULIAR_BELL_USE.play(pPlayer.level(), null, conductor.getX(), conductor.getY(), conductor.getZ(), .5f, 1.1f);
            return InteractionResult.SUCCESS;
        }
        return super.interactLivingEntity(pStack, pPlayer, pInteractionTarget, pUsedHand);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        if (context.getPlayer() == null)
            return InteractionResult.FAIL;
        return use(context.getLevel(), context.getPlayer(), context.getHand()).getResult();
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
            CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
            CompoundTag stackTag = customData.copyTag();
            if (!stackTag.hasUUID("SelectedConductor"))
                return InteractionResultHolder.fail(stack);
            UUID conductorId = stackTag.getUUID("SelectedConductor");
            if (player.isShiftKeyDown()) {
                stackTag.remove("SelectedConductor");
                stack.set(DataComponents.CUSTOM_DATA, CustomData.of(stackTag));
                AllSoundEvents.CONTROLLER_CLICK.play(level, null, player.blockPosition(), .5f, 1.1f);
                player.displayClientMessage(Component.translatable("railways.remote_lens.clear"), true);
                return InteractionResultHolder.success(stack);
            }
            Entity entity = serverLevel.getEntity(conductorId);
            if (entity instanceof ConductorEntity conductor && conductor.getJob() == ConductorEntity.Job.SPY) {
                return conductor.startViewing(serverPlayer) ? InteractionResultHolder.success(stack) : InteractionResultHolder.fail(stack);
            } else {
                return InteractionResultHolder.fail(stack);
            }
        } else {
            return InteractionResultHolder.success(stack);
        }
    }
}
