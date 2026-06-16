package illusnow.tjchase.client.particle;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.state.QuadParticleRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.jspecify.annotations.Nullable;

public class TJChaseBuffParticle extends SingleQuadParticle {
    private static final float LERP_TICKS = 3;

    public TJChaseBuffParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, TextureAtlasSprite sprite) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed, sprite);
        lifetime = 8 + random.nextInt(5);
        quadSize = 0.35F;
        gravity = 0;
        friction = 1;
        hasPhysics = false;
        xd = 0;
        yd = ySpeed;
        zd = 0;
    }

    @Override
    public void extract(QuadParticleRenderState reusedState, Camera camera, float partialTick) {
        super.extract(reusedState, camera, partialTick);
        float age = this.age + partialTick;
        if (age < LERP_TICKS) {
            setAlpha(smooth(age / LERP_TICKS));
        } else if (age > lifetime - LERP_TICKS) {
            setAlpha(smooth((lifetime - age) / LERP_TICKS));
        } else {
            setAlpha(1);
        }
    }

    @Override
    public FacingCameraMode getFacingCameraMode() {
        return FacingCameraMode.LOOKAT_Y;
    }

    @Override
    protected Layer getLayer() {
        return Layer.TRANSLUCENT;
    }

    public static float smooth(float alpha) {
        return Mth.clamp(1 - (alpha - 1) * (alpha - 1), 0, 1);
    }

    public static class Provider implements ParticleProvider<ColorParticleOption> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public @Nullable Particle createParticle(ColorParticleOption particleType, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, RandomSource random) {
            TJChaseBuffParticle particle = new TJChaseBuffParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites.get(random));
            particle.setColor(particleType.getRed(), particleType.getGreen(), particleType.getBlue());
            particle.setAlpha(particleType.getAlpha());
            return particle;
        }
    }
}
