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

package illusnow.tjchase.entity;

import com.mojang.logging.LogUtils;
import illusnow.tjchase.sound.ModSoundEvents;
import illusnow.tjchase.util.Utils;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Objects;

public class Cocoon extends PlacedEntity implements GeoEntity {
    private static final String HIT_CONTROLLER_NAME = "Hit";
    private static final String HIT_N_NAME = "hitN";
    private static final String HIT_S_NAME = "hitS";
    private static final String HIT_E_NAME = "hitE";
    private static final String HIT_W_NAME = "hitW";
    private static final String LIFE_TAG = "Life";
    private static final String STORED_ENTITY_TYPE_TAG = "StoredEntityType";
    private static final String ENTITY_DATA_TAG = "AdditionalEntityData";
    private static final String SIZE_TAG = "Size";
    private static final int MAX_LIFE = 200;
    private static final int LIFE_INCREASE_AMOUNT_ON_HIT = 100;
    private static final int ANIMATION_LENGTH = 10;
    private static final float MIN_SIZE = 0.5F;
    private static final float MAX_SIZE = 4.0F;
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final EntityDataAccessor<Float> DATA_SIZE = SynchedEntityData.defineId(Cocoon.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_DAMAGED = SynchedEntityData.defineId(Cocoon.class, EntityDataSerializers.BOOLEAN);
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    public static final RawAnimation HIT_N = RawAnimation.begin().thenPlay(HIT_N_NAME);
    public static final RawAnimation HIT_S = RawAnimation.begin().thenPlay(HIT_S_NAME);
    public static final RawAnimation HIT_E = RawAnimation.begin().thenPlay(HIT_E_NAME);
    public static final RawAnimation HIT_W = RawAnimation.begin().thenPlay(HIT_W_NAME);
    private int life;
    private int animationCooldownTicks;
    @Nullable
    protected EntityType<? extends Mob> storedEntityType;
    private CompoundTag tag = new CompoundTag();

    public Cocoon(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_SIZE, 1.0F);
        builder.define(DATA_DAMAGED, false);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (key == DATA_SIZE) {
            refreshDimensions();
        }
    }

    @Override
    public final EntityDimensions getDimensions(Pose pose) {
        return super.getDimensions(pose).scale(getSize());
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide()) {
            life++;
            if (life >= MAX_LIFE / 2) {
                setDamaged(true);
            }
            if (life >= MAX_LIFE) {
                if (storedEntityType != null) {
                    try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(LOGGER)) {
                        Mob mob = (Mob) EntityType.loadEntityRecursive(storedEntityType, TagValueInput.create(reporter, level().registryAccess(), tag), level(), EntitySpawnReason.CONVERSION, EntityProcessor.NOP);
                        if (mob != null) {
                            mob.copyPosition(this);
                            if (!Utils.canFly(mob)) {
                                mob.snapTo(Utils.tryMoveDownToGround(mob.level(), mob.position(), 12));
                            }
                            mob.spawnAnim();
                            mob.addEffect(new MobEffectInstance(MobEffects.WEAVING, 20 * 5));
                            mob.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 20 * 5));
                            mob.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 20 * 5));
                            level().addFreshEntity(mob);
                        }
                    }
                }
                spawnCobwebParticlesAroundSelf();
                playSound(ModSoundEvents.COCOON_BROKEN.get(), 1, random.triangle(1, 0.2F));
                discard();
                return;
            }
            if (animationCooldownTicks > 0) {
                animationCooldownTicks--;
            }
            if (animationCooldownTicks == 0 && random.nextDouble() * MAX_LIFE * 5 < life) {
                switch (random.nextInt(4)) {
                    case 0 -> triggerAnim(HIT_CONTROLLER_NAME, HIT_N_NAME);
                    case 1 -> triggerAnim(HIT_CONTROLLER_NAME, HIT_S_NAME);
                    case 2 -> triggerAnim(HIT_CONTROLLER_NAME, HIT_E_NAME);
                    case 3 -> triggerAnim(HIT_CONTROLLER_NAME, HIT_W_NAME);
                }
                animationCooldownTicks = ANIMATION_LENGTH;
            }
        }
    }

    public void spawnCobwebParticlesAroundSelf() {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }
        serverLevel.sendParticles(
                ParticleTypes.ITEM_COBWEB,
                getX(), getY(0.4), getZ(),
                (int) (30 * random.triangle(1, 0.2F) * getSize()),
                getBbWidth() * 0.4, getBbHeight() * 0.4, getBbWidth() * 0.4,
                0.08
        );
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.putInt(LIFE_TAG, life);
        output.store(ENTITY_DATA_TAG, CompoundTag.CODEC, tag);
        if (storedEntityType != null) {
            Identifier key = entityTypes().getKey(storedEntityType);
            Objects.requireNonNull(key);
            output.putString(STORED_ENTITY_TYPE_TAG, key.toString());
        }
        output.putFloat(SIZE_TAG, getSize());
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        life = input.getIntOr(LIFE_TAG, 0);
        if (life >= MAX_LIFE / 2) {
            setDamaged(true);
        }
        tag = input.read(ENTITY_DATA_TAG, CompoundTag.CODEC).orElseGet(CompoundTag::new);
        try {
            storedEntityType = (EntityType<? extends Mob>) input.getString(STORED_ENTITY_TYPE_TAG).map(Identifier::tryParse).map(entityTypes()::getValue).orElse(null);
        } catch (RuntimeException e) {
            LOGGER.warn("Failed to load storedEntityType", e);
            storedEntityType = null;
        }
        setSize(input.getFloatOr(SIZE_TAG, 1));
    }

    private Registry<EntityType<?>> entityTypes() {
        return level().registryAccess().lookupOrThrow(Registries.ENTITY_TYPE);
    }

    @Override
    protected void onHit(ServerLevel level, DamageSource damageSource, float amount) {
        if (damageSource.getDirectEntity() != null) {
            playHitAnimation(damageSource.getDirectEntity());
        }
        if (!damageSource.is(DamageTypeTags.NO_KNOCKBACK)) {
            double dx = 0.0;
            double dz = 0.0;
            if (damageSource.getDirectEntity() instanceof Projectile projectile) {
                dx = projectile.getDeltaMovement().x;
                dz = projectile.getDeltaMovement().z;
            } else if (damageSource.getSourcePosition() != null) {
                dx = getX() - damageSource.getSourcePosition().x();
                dz = getZ() - damageSource.getSourcePosition().z();
            }
            dx = dx / Math.sqrt(dx * dx + dz * dz);
            dz = dz / Math.sqrt(dx * dx + dz * dz);
            setDeltaMovement(getDeltaMovement().add(dx * 0.1, 0.05, dz * 0.1));
        }
        life += LIFE_INCREASE_AMOUNT_ON_HIT;
    }

    public static boolean isConvertible(Mob mob) {
        if (!(mob instanceof Enemy)) {
            return false;
        }
        if (mob.getType().is(Tags.EntityTypes.BOSSES)) {
            return false;
        }
        if (mob.isInvulnerable() || !mob.attackable()) {
            return false;
        }
        return mob.isAlive();
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static Cocoon convert(Mob mob) {
        if (mob.level().isClientSide()) {
            return null;
        }
        ServerLevel level = (ServerLevel) mob.level();
        mob.getPassengers().forEach(Entity::stopRiding);
        mob.stopRiding();

        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(LOGGER)) {
            TagValueOutput output = TagValueOutput.createWithContext(reporter, level.registryAccess());
            mob.save(output);
            Cocoon cocoon = ModEntities.COCOON.get().create(level, EntitySpawnReason.CONVERSION);
            if (cocoon == null) {
                return null;
            }
            cocoon.storedEntityType = (EntityType<? extends Mob>) mob.getType();
            cocoon.tag = output.buildResult();
            cocoon.copyPosition(mob);
            cocoon.setSize(calculateSize(mob, cocoon));
            if (level.addFreshEntity(cocoon)) {
                mob.discard();
            }
            return cocoon;
        }
    }

    private static float calculateSize(Entity entity, Entity cocoon) {
        float heightFactor = entity.getBbHeight() / cocoon.getBbHeight();
        float widthFactor = entity.getBbWidth() / cocoon.getBbWidth();
        float coefficient = 1 / (Mth.SQRT_OF_TWO - 1);
        heightFactor = coefficient * Mth.sqrt(heightFactor + 1) - coefficient;
        return Math.clamp(Mth.sqrt(heightFactor * widthFactor), MIN_SIZE, MAX_SIZE);
    }

    private void playHitAnimation(Entity attacker) {
        Utils.calculateHitAngle(attacker, getLookAngle()).ifPresent(angle -> {
            if (angle < 45 || angle >= 315) {
                triggerAnim(HIT_CONTROLLER_NAME, HIT_N_NAME);
            } else if (angle < 135) {
                triggerAnim(HIT_CONTROLLER_NAME, HIT_E_NAME);
            } else if (angle < 225) {
                triggerAnim(HIT_CONTROLLER_NAME, HIT_S_NAME);
            } else {
                triggerAnim(HIT_CONTROLLER_NAME, HIT_W_NAME);
            }
            animationCooldownTicks = ANIMATION_LENGTH;
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(HIT_CONTROLLER_NAME, test -> PlayState.STOP)
                .triggerableAnim(HIT_N_NAME, HIT_N)
                .triggerableAnim(HIT_S_NAME, HIT_S)
                .triggerableAnim(HIT_E_NAME, HIT_E)
                .triggerableAnim(HIT_W_NAME, HIT_W)
        );
    }

    public float getSize() {
        return entityData.get(DATA_SIZE);
    }

    public void setSize(float size) {
        entityData.set(DATA_SIZE, Math.clamp(size, MIN_SIZE, MAX_SIZE));
    }

    public boolean isDamaged() {
        return entityData.get(DATA_DAMAGED);
    }

    private void setDamaged(boolean damaged) {
        entityData.set(DATA_DAMAGED, damaged);
    }

    @Override
    public boolean isPushable() {
        return isAlive();
    }

    @Override
    protected double getInertia() {
        return 0.85;
    }

    @Override
    protected double getDefaultGravity() {
        return 0.1;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }
}
