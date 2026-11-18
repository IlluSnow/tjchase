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

import illusnow.tjchase.entity.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class GreenRascal extends AbstractRascal implements Bounceable {
    private static final double INERTIA = 0.9975;
    private static final double INERTIA_WATER = 0.96;
    private static final double INERTIA_HIT = 0.9;
    private static final double GRAVITY = 0.03;
    private static final double SETTLE_POSITION_DELTA_SQR = 1E-4;
    private static final double SETTLE_SPEED_SQR = 0.01;
    private static final double SETTLE_VERTICAL_SPEED = 0.1;
    private static final double PLAY_SOUND_SPEED_SQR = 0.1;
    private static final double COLLISION_EPSILON = 1E-4;
    private static final int SETTLE_TICKS = 2;
    private int slowMotionTicks;
    private boolean settling;
    private boolean rotationChangedByDeflection;

    public GreenRascal(EntityType<? extends GreenRascal> type, Level level) {
        super(type, level);
    }

    private GreenRascal(Entity owner, Level level) {
        super(ModEntities.GREEN_RASCAL.get(), owner, level);
    }

    private GreenRascal(Level level, double x, double y, double z) {
        super(ModEntities.GREEN_RASCAL.get(), level, x, y, z);
    }

    public static GreenRascal fromOwner(ServerLevel level, LivingEntity owner, ItemStack stack) {
        return new GreenRascal(owner, level);
    }

    public static GreenRascal fromPos(Level level, double x, double y, double z, ItemStack stack) {
        return new GreenRascal(level, x, y, z);
    }

    @Override
    public void tick() {
        Vec3 prevPosition = position();
        rotationChangedByDeflection = false;
        super.tick();
        checkMovement(prevPosition);
    }

    private void checkMovement(Vec3 prevPosition) {
        if (isVanishing()) {
            settling = true;
            slowMotionTicks = SETTLE_TICKS;
            setDeltaMovement(Vec3.ZERO);
            setXRot(0);
            setOldRot();
            return;
        }

        if (!level().isClientSide()) {
            Vec3 movement = getDeltaMovement();
            boolean grounded = isGrounded();
            boolean tooSmallPositionDelta = prevPosition.distanceToSqr(position()) <= SETTLE_POSITION_DELTA_SQR;
            boolean settlingMotion = (grounded || tooSmallPositionDelta || settling) && movement.lengthSqr() <= SETTLE_SPEED_SQR && Math.abs(movement.y) <= SETTLE_VERTICAL_SPEED;
            if (settlingMotion) {
                if (slowMotionTicks < SETTLE_TICKS) {
                    slowMotionTicks++;
                }
                settling = true;
                setXRot(0.0F);
                setOldRot();
                if (slowMotionTicks >= SETTLE_TICKS) {
                    settleAndVanish();
                }
            } else {
                slowMotionTicks = 0;
                settling = false;
            }
        }
    }

    @Override
    protected void updateDamage(@Nullable Entity owner) {
        boolean lowHealth = false;
        if (owner instanceof LivingEntity livingOwner) {
            lowHealth = livingOwner.getHealth() <= livingOwner.getMaxHealth() * 0.2F;
        }
        setDamage(lowHealth ? 24 : 4);
    }

    @Override
    protected void postHurt(Entity entity) {}

    @Override
    protected ProjectileDeflection hitTargetOrDeflectSelf(HitResult hitResult) {
        ProjectileDeflection projectileDeflection0 = super.hitTargetOrDeflectSelf(hitResult);
        if (projectileDeflection0 != ProjectileDeflection.NONE) {
            reapplyPositionAndRotationAfterDeflecting(hitResult);
            return projectileDeflection0;
        }

        Vec3 movement = getDeltaMovement();
        if (hitResult instanceof BlockHitResult blockHitResult) {
            Vec3 normal = getNormal(blockHitResult);
            if (normal.y > 0.0 && movement.lengthSqr() <= SETTLE_SPEED_SQR) {
                reapplyPositionAndRotationAfterDeflecting(hitResult);
                settleAndVanish();
                return ProjectileDeflection.NONE;
            }
            if (movement.dot(normal) >= -1E-5) {
                reapplyPositionAndRotationAfterDeflecting(hitResult);
                return ProjectileDeflection.NONE;
            }
        }

        ProjectileDeflection deflection = getDeflectionFromHitResultType(hitResult, movement);
        if (deflect(deflection, null, owner, false)) {
            setDeltaMovement(getDeltaMovement().scale(INERTIA_HIT));
            switch (hitResult.getType()) {
                case BLOCK -> {
                    if (getDeltaMovement().lengthSqr() > PLAY_SOUND_SPEED_SQR) {
                        playHitSound(0.25F);
                    }
                }
                case ENTITY -> playHitSound(0.5F);
            }
            reapplyPositionAndRotationAfterDeflecting(hitResult);
            return deflection;
        }
        return ProjectileDeflection.NONE;
    }

    private ProjectileDeflection getDeflectionFromHitResultType(HitResult hitResult, Vec3 deltaMovement) {
        if (hitResult instanceof BlockHitResult blockHitResult) {
            return Bounceable.getProjectileDeflection(blockHitResult.isInside() && isGrounded() ? Direction.UP : blockHitResult.getDirection());
        }
        if (hitResult instanceof EntityHitResult entityHitResult) {
            Entity target = entityHitResult.getEntity();
            Vec3 start = position();
            AABB hitBox = target.getBoundingBox().inflate(0.3);
            Vec3 impactLocation = hitBox.contains(start)
                    ? null
                    : hitBox.clip(start, start.add(deltaMovement)).orElse(null);
            EntityHitResult hitWithLocation = impactLocation == null
                    ? entityHitResult
                    : new EntityHitResult(target, impactLocation);
            return Bounceable.getProjectileDeflection(deltaMovement, hitWithLocation);
        }
        return Bounceable.getProjectileDeflection(deltaMovement, null);
    }

    private void reapplyPositionAndRotationAfterDeflecting(@Nullable HitResult hitResult) {
        Vec3 deltaMovement = getDeltaMovement();
        moveProjectileOutOfBlock(hitResult, deltaMovement);
        reapplyPosition();
        if (rotationChangedByDeflection) {
            updateRotationWhenBouncing();
        }
        setOldPosAndRot();
    }

    private void moveProjectileOutOfBlock(@Nullable HitResult hitResult, Vec3 deltaMovement) {
        if (hitResult instanceof BlockHitResult blockHitResult && getNormal(blockHitResult).y > 0.0) {
            double minimumY = getCollisionSurfaceY(blockHitResult);
            if (getY() < minimumY) {
                setPos(getX(), minimumY, getZ());
            }
        }
        if (deltaMovement.lengthSqr() > 1E-7 && !level().noBlockCollision(this, getBoundingBox())) {
            Vec3 correction = hitResult instanceof BlockHitResult blockHitResult
                    ? getNormal(blockHitResult)
                    : deltaMovement.normalize();
            double distance = 1E-4;
            double maxDistance = Math.max(0.25, Math.max(getBbWidth(), getBbHeight()));
            double freeDistance = -1;
            while (distance <= maxDistance) {
                if (level().noBlockCollision(this, getBoundingBox().move(correction.scale(distance)))) {
                    freeDistance = distance;
                    break;
                }
                distance *= 2;
            }

            if (freeDistance >= 0) {
                double blockedDistance = freeDistance / 2;
                for (int i = 0; i < 6; i++) {
                    double middle = (blockedDistance + freeDistance) * 0.5;
                    if (level().noBlockCollision(this, getBoundingBox().move(correction.scale(middle)))) {
                        freeDistance = middle;
                    } else {
                        blockedDistance = middle;
                    }
                }
                setPos(position().add(correction.scale(freeDistance + COLLISION_EPSILON)));
            }
        }
    }

    @Override
    public void updateRotationWhenBouncing() {
        updateRotationFromMovement(false);
        setOldRot();
    }

    @Override
    protected void updateRotation() {
        updateRotationFromMovement(true);
    }

    private void updateRotationFromMovement(boolean smooth) {
        Vec3 deltaMovement = getDeltaMovement();
        double horizontalDistance = deltaMovement.horizontalDistance();

        // A rascal that is only sliding along the ground keeps its current
        // orientation.  Its velocity may change slightly because of inertia or
        // gravity, but that is not a turn and should not rotate the model.
        if (isGrounded() && !rotationChangedByDeflection) {
            return;
        }

        if (horizontalDistance > 1E-5) {
            float targetYRot = (float) (Mth.atan2(deltaMovement.x, deltaMovement.z) * 180 / Math.PI);
            setYRot(smooth ? lerpRotation(yRotO, targetYRot) : targetYRot);
        }

        float targetXRot = calculateXRot(horizontalDistance, deltaMovement);
        setXRot(smooth ? lerpRotation(xRotO, targetXRot) : targetXRot);
    }

    private float calculateXRot(double horizontalDistance, Vec3 deltaMovement) {
        boolean grounded = isGrounded();
        // Near the ground, gravity can make the vertical component change sign
        // from tick to tick.  Keeping a nearly horizontal trajectory level avoids
        // the model repeatedly pitching up and down as it settles.
        boolean nearlyHorizontal = horizontalDistance > 1.0E-5 && Math.abs(deltaMovement.y) < 0.1;
        return (settling || grounded || nearlyHorizontal) ? 0.0F : (float) (Mth.atan2(deltaMovement.y, horizontalDistance) * 180 / Math.PI);
    }

    private boolean isGrounded() {
        return onGround() || !level().noBlockCollision(this, getBoundingBox().move(0, -0.2, 0));
    }

    private void settleAndVanish() {
        settling = true;
        slowMotionTicks = SETTLE_TICKS;
        setDeltaMovement(Vec3.ZERO);
        setXRot(0);
        setOldRot();
        if (!level().isClientSide() && !isVanishing()) {
            startVanishing();
        }
    }

    private Vec3 getNormal(BlockHitResult hitResult) {
        Vec3 normal = new Vec3(
                hitResult.getDirection().getStepX(),
                hitResult.getDirection().getStepY(),
                hitResult.getDirection().getStepZ()
        );
        if (normal.y <= 0.0 && hitResult.isInside() && isGrounded()) {
            return new Vec3(0.0, 1.0, 0.0);
        }
        return normal;
    }

    private double getCollisionSurfaceY(BlockHitResult hitResult) {
        BlockPos blockPos = hitResult.getBlockPos();
        VoxelShape shape = level().getBlockState(blockPos).getCollisionShape(level(), blockPos);
        return shape.isEmpty() ? hitResult.getLocation().y : blockPos.getY() + shape.bounds().maxY;
    }

    @Override
    public boolean deflect(ProjectileDeflection deflection, @Nullable Entity entity, @Nullable EntityReference<Entity> owner, boolean deflectionByPlayer) {
//        settling = false;
//        slowMotionTicks = 0;
        rotationChangedByDeflection = true;
        deflection.deflect(this, entity, random);
        if (!level().isClientSide()) {
//          Owner unchanged
            onDeflection(deflectionByPlayer);
        }
        return true;
    }

    @Override
    protected double getInertia() {
        return INERTIA;
    }

    @Override
    protected double getWaterInertia() {
        return INERTIA_WATER;
    }

    @Override
    protected double getDefaultGravity() {
        return GRAVITY;
    }
}
