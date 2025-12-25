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

package com.railwayteam.railways.content.smokestack.particles;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.TextureSheetParticle;

@OnlyIn(Dist.CLIENT)
public abstract class CustomAnimatedTextureSheetParticle extends TextureSheetParticle {
    protected CustomAnimatedTextureSheetParticle(ClientLevel clientLevel, double d, double e, double f) {
        super(clientLevel, d, e, f);
    }

    protected CustomAnimatedTextureSheetParticle(ClientLevel clientLevel, double d, double e, double f, double g, double h, double i) {
        super(clientLevel, d, e, f, g, h, i);
    }

    protected abstract double getAnimationProgress();

    protected int frameWidthFactor() {
        return 1;
    }

    protected int frameHeightFactor() {
        return 1;
    }

    @Override
    protected float getU0() {
        return this.sprite.getU0();
    }

    @Override
    protected float getU1() {
        return this.sprite.getU1();
    }

    @Override
    protected float getV0() {
        int spriteHeight = this.sprite.contents().height();
        int spriteWidth = this.sprite.contents().width();
        int frames = (spriteHeight * frameWidthFactor()) / (spriteWidth * frameHeightFactor());
        int frameNumber = Math.min((int) (frames * getAnimationProgress()), frames - 1);
        
        // Calculate the V coordinate for the top of the current frame
        float vMin = this.sprite.getV0();
        float vMax = this.sprite.getV1();
        float frameSize = (vMax - vMin) / frames;
        
        return vMin + (frameNumber * frameSize);
    }

    @Override
    protected float getV1() {
        int spriteHeight = this.sprite.contents().height();
        int spriteWidth = this.sprite.contents().width();
        int frames = (spriteHeight * frameWidthFactor()) / (spriteWidth * frameHeightFactor());
        int frameNumber = Math.min((int) (frames * getAnimationProgress()), frames - 1);
        
        // Calculate the V coordinate for the bottom of the current frame
        float vMin = this.sprite.getV0();
        float vMax = this.sprite.getV1();
        float frameSize = (vMax - vMin) / frames;
        
        return vMin + ((frameNumber + 1) * frameSize);
    }
}
