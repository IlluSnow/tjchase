/*
 * Copyright 2026 IlluSnow
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package illusnow.tjchase.client.particle;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.state.QuadParticleRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

import static illusnow.tjchase.client.particle.TJChaseBuffParticle.smooth;

public class HarpPlayedNoteParticle extends SingleQuadParticle {
    private static final float FADE_THRESHOLD = 0.6F;

    public HarpPlayedNoteParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, boolean smallerSize, TextureAtlasSprite sprite) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed, sprite);
        lifetime = 10;
        gravity = 0;
        friction = 0.96F;
        hasPhysics = false;
        yd -= 0.1F;
        setColor(0.9F, 1, 1);
        if (smallerSize) {
            quadSize *= 1.8F;
        }
    }

    @Override
    public int getLightColor(float partialTick) {
        int lightColor = super.getLightColor(partialTick);
        int blockLightColor = 240;
        int skyLightColor = lightColor >> 16 & 0xFF;
        return blockLightColor | skyLightColor << 16;
    }
    @Override
    public void extract(QuadParticleRenderState reusedState, Camera camera, float partialTick) {
        super.extract(reusedState, camera, partialTick);
        float age = this.age + partialTick;
        if (age > lifetime * FADE_THRESHOLD) {
            setAlpha(smooth((lifetime - age) / lifetime * (1 - FADE_THRESHOLD)));
        } else {
            setAlpha(1);
        }
    }

    @Override
    protected Layer getLayer() {
        return Layer.TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType particleType, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, RandomSource random) {
            TextureAtlasSprite chosen = random.nextBoolean() ? sprites.first() : sprites.get(random);
            boolean smallerSize = chosen == sprites.get(1, 1); // THe last sprite
            return new HarpPlayedNoteParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, smallerSize, chosen);
        }
    }
}
