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

package com.railwayteam.railways.neoforge.client.track;

import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions;

import java.util.ArrayList;
import java.util.List;

/**
 * Spawns destroy/hit particles over the full block shape, including shapes that extend outside the block's 0..1 bounds.
 *
 * Vanilla destroy particle spawning effectively assumes in-bounds shapes; custom tracks use out-of-bounds voxel shapes
 * (e.g. long/offset track pieces), so we need to sample from all AABBs to get uniform coverage.
 */
public class FullShapeDestroyEffects implements IClientBlockExtensions {
    public static final FullShapeDestroyEffects INSTANCE = new FullShapeDestroyEffects();

    private static final int DESTROY_PARTICLE_COUNT = 64;

    private FullShapeDestroyEffects() {
    }

    @Override
    public boolean addDestroyEffects(BlockState state, Level level, BlockPos pos, ParticleEngine manager) {
        if (state.getRenderShape() == net.minecraft.world.level.block.RenderShape.INVISIBLE)
            return true;

        spawnParticlesFromShape(state, level, pos, level.getRandom(), DESTROY_PARTICLE_COUNT);
        return true;
    }

    private static void spawnParticlesFromShape(BlockState state, Level level, BlockPos pos, RandomSource random, int count) {
        VoxelShape shape = state.getShape(level, pos);
        List<AABB> aabbs = new ArrayList<>(shape.toAabbs());
        if (aabbs.isEmpty()) {
            aabbs.add(shape.bounds());
        }

        // Precompute weighted selection by volume.
        double totalVolume = 0;
        double[] cumulative = new double[aabbs.size()];
        for (int i = 0; i < aabbs.size(); i++) {
            AABB bb = aabbs.get(i);
            double v = bb.getXsize() * bb.getYsize() * bb.getZsize();
            // Avoid zero-volume boxes.
            v = Math.max(v, 1e-6);
            totalVolume += v;
            cumulative[i] = totalVolume;
        }

        BlockParticleOption particle = new BlockParticleOption(ParticleTypes.BLOCK, state);

        for (int i = 0; i < count; i++) {
            AABB bb = pickAabbWeighted(aabbs, cumulative, totalVolume, random);

            double x = pos.getX() + Mth.lerp(random.nextDouble(), bb.minX, bb.maxX);
            double y = pos.getY() + Mth.lerp(random.nextDouble(), bb.minY, bb.maxY);
            double z = pos.getZ() + Mth.lerp(random.nextDouble(), bb.minZ, bb.maxZ);

            double vx = (random.nextDouble() - 0.5) * 0.15;
            double vy = (random.nextDouble() - 0.5) * 0.15;
            double vz = (random.nextDouble() - 0.5) * 0.15;

            level.addParticle(particle, x, y, z, vx, vy, vz);
        }
    }

    private static AABB pickAabbWeighted(List<AABB> aabbs, double[] cumulative, double totalVolume, RandomSource random) {
        double r = random.nextDouble() * totalVolume;
        int idx = java.util.Arrays.binarySearch(cumulative, r);
        if (idx < 0) {
            idx = -idx - 1;
        }
        idx = Mth.clamp(idx, 0, aabbs.size() - 1);
        return aabbs.get(idx);
    }
}
