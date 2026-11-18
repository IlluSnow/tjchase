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

package illusnow.tjchase.entity.projectile;

import illusnow.tjchase.sound.ModSoundEvents;
import illusnow.tjchase.world.ModDamageSources;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public abstract class AbstractRascal extends ThrowableProjectile implements Seekable, GeoEntity {
    public static final String VANISH_CONTROLLER_NAME = "Vanish";
    public static final String VANISH_NAME = "vanish";
    public static final RawAnimation VANISH = RawAnimation.begin().thenLoop("vanish");
    private static final String LIFE_TAG = "Life";
    private static final String DAMAGE_TAG = "Life";
    private static final String VANISHING_TAG = "Vanishing";
    private static final int MAX_LIFE = 600;
    private static final int MAX_VANISH_TICKS = 5;
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private float damage;
    protected int life;
    private int vanishTicks;
    private boolean vanishing;

    protected AbstractRascal(EntityType<? extends AbstractRascal> type, Level level) {
        super(type, level);
        updateDamage(null);
    }

    protected AbstractRascal(EntityType<? extends AbstractRascal> type, Entity owner, Level level) {
        super(type, owner.getX(), owner.getEyeY() - 0.1F, owner.getZ(), level);
        setOwner(owner);
        setRot(owner.getYRot(), owner.getXRot());
        updateDamage(owner);
    }

    protected AbstractRascal(EntityType<? extends AbstractRascal> type, Level level, double x, double y, double z) {
        super(type, x, y, z, level);
        updateDamage(null);
    }

    protected abstract void updateDamage(@Nullable Entity owner);

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {}

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide()) {
            life++;
            if (life > getMaxLife() && !isVanishing()) {
                startVanishing();
            }
            if (isVanishing()) {
                vanishTicks--;
                if (vanishTicks <= 0) {
                    discard();
                    return;
                }
            }
        }
        if (canSeek()) {
            Entity target = getTarget();
            if (target != null) {
                trySeek(target);
            }
        }
    }

    public void startVanishing() {
        vanishTicks = MAX_VANISH_TICKS;
        triggerAnim(VANISH_CONTROLLER_NAME, VANISH_NAME);
        vanishing = true;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (result.getEntity().level() instanceof ServerLevel level && !result.getEntity().isAlliedTo(getOwner())) {
            boolean hurt = result.getEntity().hurtServer(level, getOwner() == null ? ModDamageSources.rascal(this) : ModDamageSources.indirectRascal(this, getOwner()), getDamage());
            if (hurt) {
                postHurt(result.getEntity());
            }
        }
    }

    protected void playHitSound(float volume) {
        playSound(ModSoundEvents.RASCAL_HIT.get(), volume, random.triangle(1, 0.2F));
    }

    protected abstract void postHurt(Entity entity);

    @Nullable
    @Override
    public Entity getTarget() {
        return null;
    }

    @Override
    public double getSeekPower() {
        return 0;
    }

    protected int getMaxLife() {
        return MAX_LIFE;
    }

    @Override
    protected void applyInertia() {
        Vec3 movement = getDeltaMovement();
        Vec3 position = position();
        double inertia;
        if (isInWater()) {
            for (int i = 0; i < 4; i++) {
                float offset = 0.25F;
                level().addParticle(ParticleTypes.BUBBLE, position.x - movement.x * offset, position.y - movement.y * offset, position.z - movement.z * offset, movement.x, movement.y, movement.z);
            }
            inertia = getWaterInertia();
        } else {
            inertia = getInertia();
        }
        setDeltaMovement(movement.scale(inertia));
    }

    protected abstract double getInertia();

    protected abstract double getWaterInertia();

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt(LIFE_TAG, life);
        output.putFloat(DAMAGE_TAG, getDamage());
        output.putBoolean(VANISHING_TAG, isVanishing());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        life = input.getIntOr(LIFE_TAG, 0);
        setDamage(input.getFloatOr(DAMAGE_TAG, 0));
        vanishing = input.getBooleanOr(VANISHING_TAG, false);
    }

    public float getDamage() {
        return damage;
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    public boolean isVanishing() {
        return vanishing;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(VANISH_CONTROLLER_NAME, state -> PlayState.STOP).triggerableAnim(VANISH_NAME, VANISH));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }
}
