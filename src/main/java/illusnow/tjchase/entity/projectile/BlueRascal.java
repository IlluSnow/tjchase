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

import com.google.common.primitives.Ints;
import illusnow.tjchase.entity.ModEntities;
import illusnow.tjchase.entity.ModEntityDataSerializers;
import illusnow.tjchase.item.ModItems;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class BlueRascal extends AbstractRascal {
    private static final EntityDataAccessor<Integer> DATA_PHASE = SynchedEntityData.defineId(BlueRascal.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Long> DATA_START_SEEKING_GAME_TIME = SynchedEntityData.defineId(BlueRascal.class, EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Optional<EntityReference<Entity>>> DATA_TARGET = SynchedEntityData.defineId(
            BlueRascal.class, ModEntityDataSerializers.OPTIONAL_ENTITY_REFERENCE.get()
    );
    private static final String PHASE_TAG = "Phase";
    private static final String TARGET_TAG = "Target";
    private static final String SEEKING_GAME_TIME_TAG = "SeekingTime";
    private static final double MIN_SEEK_POWER = 0.8;
    private static final double MAX_SEEK_POWER = 1;
    private static final int SEEK_POWER_INCREASE_TICKS = 20;
    private static final float SECONDARY_SHOOT_POWER = 0.7F;
    private static final double START_WAITING_TARGET_MOVEMENT_LENGTH_SQR = 1E-4;
    private static final double SEARCH_TARGET_RADIUS = 8;
    private static final double VALID_TARGET_MIN_COS_ANGLE = 0.6;
    private static final float Y_ROTATION_PER_TICK = 5;
    private static final int LEVITATION_DURATION = 80;
    private static final int LEVITATION_AMPLIFIER = 0;

    public BlueRascal(EntityType<? extends BlueRascal> type, Level level) {
        super(type, level);
    }

    private BlueRascal(Entity owner, Level level) {
        super(ModEntities.BLUE_RASCAL.get(), owner, level);
    }

    private BlueRascal(Level level, double x, double y, double z) {
        super(ModEntities.BLUE_RASCAL.get(), level, x, y, z);
    }

    public static BlueRascal fromOwner(ServerLevel level, LivingEntity owner, ItemStack stack) {
        return new BlueRascal(owner, level);
    }

    public static BlueRascal fromPos(Level level, double x, double y, double z, ItemStack stack) {
        return new BlueRascal(level, x, y, z);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_PHASE, 0);
        builder.define(DATA_TARGET, Optional.empty());
        builder.define(DATA_START_SEEKING_GAME_TIME, 0L);
    }

    @Override
    protected void updateDamage(@Nullable Entity owner) {
        setDamage(8);
    }

    @Override
    protected void updateRotation() {
        if (getPhase() != Phase.WAITING_TARGET) {
            super.updateRotation();
            if (getPhase() == Phase.SENDING) {
                setXRot(getXRot() / 2);
            }
        } else {
            setXRot(0);
        }
    }

    @Override
    public void tick() {
        super.tick();
        tickPhase();
    }

    private void tickPhase() {
        Phase phase = getPhase();
        if (!level().isClientSide() && phase.canActivelySearchTarget() && getTarget() == null) {
            Entity target = findTarget();
            if (target != null) {
                setTarget(target);
                phase = switchToPhase(Phase.SEEKING);
            }
        }
        if (phase == Phase.SENDING) {
            if (!level().isClientSide() && getDeltaMovement().lengthSqr() <= START_WAITING_TARGET_MOVEMENT_LENGTH_SQR) {
                phase = switchToPhase(Phase.WAITING_TARGET);
            }
        }
        if (phase == Phase.WAITING_TARGET) {
            setYRot(wrapDegrees(getYRot(), Y_ROTATION_PER_TICK));
        }
    }

    @Override
    protected void postHurt(Entity entity) {
        if (entity instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.LEVITATION, LEVITATION_DURATION, LEVITATION_AMPLIFIER));
        }
    }

    private float wrapDegrees(float angle, float increment) {
        return Mth.wrapDegrees(angle + increment);
    }

    @Nullable
    private Entity findTarget() {
        List<Entity> availableEntities = level().getEntities(this, getBoundingBox().inflate(SEARCH_TARGET_RADIUS), this::canAttack);
        Entity target = null;
        double minDistanceSqr = SEARCH_TARGET_RADIUS * SEARCH_TARGET_RADIUS;
        for (Entity entity : availableEntities) {
            Vec3 horizontalVecToTarget = entity.position().subtract(position()).horizontal();
            Vec3 actualHorizontalLookAngle = getLookAngle().multiply(-1, -1, 1).horizontal();
            double cosAngle = horizontalVecToTarget.dot(actualHorizontalLookAngle) / horizontalVecToTarget.length() / actualHorizontalLookAngle.length();
            if (cosAngle > VALID_TARGET_MIN_COS_ANGLE && distanceToSqr(entity) < minDistanceSqr && canSee(entity)) {
                target = entity;
                minDistanceSqr = distanceToSqr(target);
            }
        }
        return target;
    }

    private boolean canSee(Entity entity) {
        Vec3 eyePosition = getEyePosition();
        Vec3 targetPosition = new Vec3(entity.getX(), entity.getY(0.5), entity.getZ());
        return level().clip(new ClipContext(eyePosition, targetPosition, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this)).getType() == HitResult.Type.MISS;
    }

    private boolean canAttack(Entity entity) {
        if (entity.isAlliedTo(getOwner())) {
            return false;
        }
        if (entity instanceof EnderMan) {
            return false;
        }
        if (getOwner() instanceof LivingEntity livingOwner) {
            if (entity == livingOwner.getLastHurtByMob() || entity == livingOwner.getLastHurtMob()) {
                return true;
            }
            if (livingOwner instanceof Enemy) {
                return entity instanceof Player || entity instanceof IronGolem;
            }
        }
        return entity instanceof Enemy || entity instanceof EndCrystal;
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!level().isClientSide()) {
            level().broadcastEntityEvent(this, EntityEvent.DEATH);
            playHitSound(0.5F);
            discard();
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        super.handleEntityEvent(id);
        if (id == EntityEvent.DEATH) {
            for (int i = 0; i < 8; i++) {
                level().addParticle(
                        new ItemParticleOption(ParticleTypes.ITEM, ModItems.BLUE_RASCAL.get().getDefaultInstance()),
                        getBoundingBox().getCenter().x,
                        getBoundingBox().getCenter().y,
                        getBoundingBox().getCenter().z,
                        (random.nextFloat() - 0.5) * 0.08,
                        (random.nextFloat() - 0.5) * 0.08,
                        (random.nextFloat() - 0.5) * 0.08
                );
            }
        }
    }

    @Override
    protected double getInertia() {
        return getPhase().getInertia();
    }

    @Override
    protected double getWaterInertia() {
        return getPhase().getInertia();
    }

    @Override
    protected double getDefaultGravity() {
        return 0;
    }

    @Override
    public double getSeekPower() {
        return getPhase() == Phase.SEEKING ? Mth.lerp((double) getSeekingTime() / SEEK_POWER_INCREASE_TICKS, MIN_SEEK_POWER, MAX_SEEK_POWER) : 0;
    }

    private Phase getPhase() {
        return Phase.byId(entityData.get(DATA_PHASE));
    }

    private void setPhase(Phase phase) {
        entityData.set(DATA_PHASE, phase.getId());
    }

    private Phase switchToPhase(Phase newPhase) {
        Phase phase = getPhase();
        if (phase.getId() >= newPhase.getId()) {
            throw new IllegalStateException("Cannot switch phase from %s to %s".formatted(phase, newPhase));
        }
        setPhase(newPhase);
        newPhase.onSetPhase(this);
        return newPhase;
    }

    private int getSeekingTime() {
        if (getPhase() != Phase.SEEKING) {
            return 0;
        }
        return Ints.saturatedCast(level().getGameTime() - entityData.get(DATA_START_SEEKING_GAME_TIME));
    }

    @Nullable
    @Override
    public Entity getTarget() {
        return entityData.get(DATA_TARGET).map(target -> EntityReference.getEntity(target, level())).orElse(null);
    }

    private void setTarget(@Nullable Entity target) {
        entityData.set(DATA_TARGET, Optional.ofNullable(EntityReference.of(target)));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt(PHASE_TAG, getPhase().getId());
        entityData.get(DATA_TARGET).ifPresent(target -> EntityReference.store(target, output, TARGET_TAG));
        output.putLong(SEEKING_GAME_TIME_TAG, entityData.get(DATA_START_SEEKING_GAME_TIME));
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        setPhase(Phase.byId(input.getIntOr(PHASE_TAG, 0)));
        entityData.set(DATA_TARGET, Optional.ofNullable(EntityReference.read(input, TARGET_TAG)));
        entityData.set(DATA_START_SEEKING_GAME_TIME, input.getLongOr(SEEKING_GAME_TIME_TAG, 0));
    }

    private enum Phase {
        SENDING(0, 0.75, true) {
            @Override
            public void onSetPhase(BlueRascal rascal) {}
        },
        WAITING_TARGET(1, 0, true) {
            @Override
            public void onSetPhase(BlueRascal rascal) {
                rascal.setDeltaMovement(Vec3.ZERO);
            }
        },
        SEEKING(2, 1, false) {
            @Override
            public void onSetPhase(BlueRascal rascal) {
                double x = rascal.getDeltaMovement().x;
                double y = rascal.getDeltaMovement().y;
                double z = rascal.getDeltaMovement().z;
                double movementLen = rascal.getDeltaMovement().length();
                if (movementLen * movementLen < START_WAITING_TARGET_MOVEMENT_LENGTH_SQR) {
                    x = -rascal.getLookAngle().x;
                    y = -rascal.getLookAngle().y;
                    z = rascal.getLookAngle().z;
                }
                rascal.shoot(x, y, z, SECONDARY_SHOOT_POWER, 0);
                rascal.entityData.set(DATA_START_SEEKING_GAME_TIME, rascal.level().getGameTime());
            }
        };

        private final int id;
        private final double inertia;
        private final boolean canActivelySearchTarget;

        Phase(int id, double inertia, boolean canActivelySearchTarget) {
            this.id = id;
            this.inertia = inertia;
            this.canActivelySearchTarget = canActivelySearchTarget;
        }

        public int getId() {
            return id;
        }

        public static Phase byId(int id) {
            return switch (id) {
                case 1 -> WAITING_TARGET;
                case 2 -> SEEKING;
                default -> SENDING;
            };
        }

        public double getInertia() {
            return inertia;
        }

        public boolean canActivelySearchTarget() {
            return canActivelySearchTarget;
        }

        public abstract void onSetPhase(BlueRascal rascal);
    }
}
