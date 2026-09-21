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

package illusnow.tjchase.entity.controllable;

import illusnow.tjchase.mixin.LivingEntityAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.UseEffects;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Iterator;
import java.util.Objects;
import java.util.stream.StreamSupport;

public final class ControllableMovementHandler {
    public static final boolean ENABLE_AUTO_JUMP = false;

    private ControllableMovementHandler() {}

    public static void updateControlledMobMovement(Controllable controllingMob, Player player, Input input) {
        LivingEntity mob = controllingMob.getSelfAsEntity();
        updateShiftAndSprint(controllingMob, input);
        float x = calculateImpulse(input.left(), input.right());
        float z = calculateImpulse(input.forward(), input.backward());
        Vec2 movementVec = modifyMovement(mob, input, new Vec2(x, z).normalized());
        mob.xxa = movementVec.x;
        mob.setSpeed(movementVec.length());
        mob.zza = movementVec.y;
        updateJump(controllingMob, input);
    }

    public static void updateShiftAndSprint(Controllable controllingMob, Input input) {
        LivingEntity mob = controllingMob.getSelfAsEntity();
        mob.setSprinting(input.sprint() && !input.shift());
        mob.setShiftKeyDown(input.shift());
    }

    public static void updateJump(Controllable controllingMob, Input input) {
        LivingEntity mob = controllingMob.getSelfAsEntity();
        float x = calculateImpulse(input.left(), input.right());
        float z = calculateImpulse(input.forward(), input.backward());
        Vec2 movementVec = modifyMovement(mob, input, new Vec2(x, z).normalized());
        if (input.jump() && mob.onGround()) {
            makeControlledMobJump(mob, movementVec.lengthSquared() > 0);
        }
    }

    public static void makeControlledMobJump(LivingEntity mob, boolean addMovement) {
        mob.jumpFromGround();
        if (((LivingEntityAccessor) mob).callGetJumpPower() > 1E-5 && addMovement) {
            // Sprinting mobs have already had a 0.2 movement boost
            addExtraJumpMovement(mob, mob.isShiftKeyDown() ? 0.06 : (mob.isSprinting() ? 0.06 : 0.2));
        }
        mob.needsSync = false;
    }

    public static Vec2 calculateMoveVector(Input input) {
        float x = ControllableMovementHandler.calculateImpulse(input.left(), input.right());
        float z = ControllableMovementHandler.calculateImpulse(input.forward(), input.backward());
        return new Vec2(x, z).normalized();
    }

    private static float calculateImpulse(boolean input, boolean otherInput) {
        if (input == otherInput) {
            return 0;
        } else {
            return input ? 1 : -1;
        }
    }

    /**
     * [VanillaCopy]
     * {@link net.minecraft.client.player.LocalPlayer}
     */
    private static Vec2 modifyMovement(LivingEntity entity, Input input, Vec2 movement) {
        if (movement.lengthSquared() == 0) {
            return movement;
        } else {
            float speedMul = 1;
            if (entity.isCrouching() || entity.isVisuallyCrawling() || input.shift()) {
                speedMul *= 0.5F; // Do not use normal sneaking speed here, otherwise it'll be too slow
            }
            Vec2 scaledMovement = movement.scale((float) entity.getAttributeValue(Attributes.MOVEMENT_SPEED) * speedMul);
            if (entity.isUsingItem() && !entity.isPassenger()) {
                scaledMovement = scaledMovement.scale(entity.getUseItem().getOrDefault(DataComponents.USE_EFFECTS, UseEffects.DEFAULT).speedMultiplier());
            }
            return scaledMovement;
        }
    }

    private static Vec2 modifyInputSpeedForSquareMovement(Vec2 movement) {
        float len = movement.length();
        if (len <= 0) {
            return movement;
        } else {
            Vec2 vec2 = movement.scale(1 / len);
            float dis = distanceToUnitSquare(vec2);
            float scale = Math.min(len * dis, 1);
            return vec2.scale(scale);
        }
    }

    private static float distanceToUnitSquare(Vec2 moveVector) {
        float absX = Math.abs(moveVector.x);
        float absY = Math.abs(moveVector.y);
        float proportion = absY > absX ? absX / absY : absY / absX;
        return Mth.sqrt(1 + Mth.square(proportion));
    }

    public static Vec3 preventFallingDownWhenSneaking(Entity entity, Vec3 vec) {
        float maxUpStep = entity.maxUpStep();
        if (!(vec.y > 0.0) && isAboveGround(entity, maxUpStep)) {
            double d0 = vec.x;
            double d1 = vec.z;
            double d2 = 0.05;
            double d3 = Math.signum(d0) * d2;

            double d4;
            for (d4 = Math.signum(d1) * d2; d0 != 0.0 && canFallAtLeast(entity, d0, 0.0, maxUpStep); d0 -= d3) {
                if (Math.abs(d0) <= d2) {
                    d0 = 0.0;
                    break;
                }
            }

            while (d1 != 0.0 && canFallAtLeast(entity, 0.0, d1, maxUpStep)) {
                if (Math.abs(d1) <= d2) {
                    d1 = 0.0;
                    break;
                }

                d1 -= d4;
            }

            while (d0 != 0.0 && d1 != 0.0 && canFallAtLeast(entity, d0, d1, maxUpStep)) {
                if (Math.abs(d0) <= d2) {
                    d0 = 0.0;
                } else {
                    d0 -= d3;
                }

                if (Math.abs(d1) <= 0.05) {
                    d1 = 0.0;
                } else {
                    d1 -= d4;
                }
            }

            return new Vec3(d0, vec.y, d1);
        } else {
            return vec;
        }
    }

    private static boolean isAboveGround(Entity entity, float maxUpStep) {
        return entity.onGround() || entity.fallDistance < maxUpStep && !canFallAtLeast(entity, 0.0, 0.0, maxUpStep - entity.fallDistance);
    }

    private static boolean canFallAtLeast(Entity entity, double x, double z, double distance) {
        AABB aabb = entity.getBoundingBox();
        return entity.level()
                .noCollision(
                        entity,
                        new AABB(
                                aabb.minX + 1.0E-7 + x,
                                aabb.minY - distance - 1.0E-7,
                                aabb.minZ + 1.0E-7 + z,
                                aabb.maxX - 1.0E-7 + x,
                                aabb.minY,
                                aabb.maxZ - 1.0E-7 + z
                        )
                );
    }

    public static void addExtraJumpMovement(Entity entity, double power) {
        float rad = entity.getYRot() * (float) (Math.PI / 180.0);
        entity.addDeltaMovement(new Vec3(-Mth.sin(rad) * power, 0.0, Mth.cos(rad) * power));
    }

    public static void updateAutoJumpIfPossible(LivingEntity entity, float movementX, float movementZ, Input input, Runnable autoJumpTimeSetter) {
        Vec3 position = entity.position();
        Vec3 targetPosition = position.add(movementX, 0.0, movementZ);
        Vec3 movement = new Vec3(movementX, 0.0, movementZ);
        float speed = entity.getSpeed();
        float movementLenSqr = (float) movement.lengthSqr();
        if (movementLenSqr <= 0.001F) {
            Vec2 inputMoveVector = calculateMoveVector(input);
            float dx = speed * inputMoveVector.x;
            float dz = speed * inputMoveVector.y;
            float xRad = Mth.sin(entity.getYRot() * (float) (Math.PI / 180.0));
            float zRad = Mth.cos(entity.getYRot() * (float) (Math.PI / 180.0));
            movement = new Vec3(dx * zRad - dz * xRad, movement.y, dz * zRad + dx * xRad);
            movementLenSqr = (float) movement.lengthSqr();
            if (movementLenSqr <= 0.001F) {
                return;
            }
        }

        float movementLenInv = Mth.invSqrt(movementLenSqr);
        Vec3 normalizedMovement = movement.scale(movementLenInv);
        Vec3 forward = entity.getForward();
        float forwardLen = (float) (forward.x * normalizedMovement.x + forward.z * normalizedMovement.z);
        if (forwardLen >= -0.15F) {
            CollisionContext collisionContext = CollisionContext.of(entity);
            BlockPos maxPos = BlockPos.containing(entity.getX(), entity.getBoundingBox().maxY, entity.getZ());
            BlockState state = entity.level().getBlockState(maxPos);
            if (state.getCollisionShape(entity.level(), maxPos, collisionContext).isEmpty()) {
                maxPos = maxPos.above();
                BlockState aboveState = entity.level().getBlockState(maxPos);
                if (aboveState.getCollisionShape(entity.level(), maxPos, collisionContext).isEmpty()) {
                    float maxCheckingLenMul = 7.0F;
                    float maxHeight = 1.2F;
                    if (entity.hasEffect(MobEffects.JUMP_BOOST)) {
                        maxHeight += (Objects.requireNonNull(entity.getEffect(MobEffects.JUMP_BOOST)).getAmplifier() + 1) * 0.75F;
                    }

                    float checkingLen = Math.max(speed * maxCheckingLenMul, 1.0F / movementLenInv);
                    Vec3 checkingBound = targetPosition.add(normalizedMovement.scale(checkingLen));
                    float bbWidth = entity.getBbWidth();
                    float bbHeight = entity.getBbHeight();
                    AABB aabb = new AABB(position, checkingBound.add(0.0, bbHeight, 0.0)).inflate(bbWidth, 0.0, bbWidth);
                    Vec3 addedPosition = position.add(0.0, 0.51F, 0.0);
                    checkingBound = checkingBound.add(0.0, 0.51F, 0.0);
                    Vec3 besidePosition = normalizedMovement.cross(new Vec3(0.0, 1.0, 0.0));
                    Vec3 checkDelta = besidePosition.scale(bbWidth * 0.5F);
                    Vec3 startMin = addedPosition.subtract(checkDelta);
                    Vec3 endMin = checkingBound.subtract(checkDelta);
                    Vec3 startMax = addedPosition.add(checkDelta);
                    Vec3 endMax = checkingBound.add(checkDelta);
                    Iterable<VoxelShape> collisions = entity.level().getCollisions(entity, aabb);
                    Iterator<AABB> collisionAABBItr = StreamSupport.stream(collisions.spliterator(), false)
                            .flatMap(shape -> shape.toAabbs().stream())
                            .iterator();
                    float maxY = Float.MIN_VALUE;

                    while (collisionAABBItr.hasNext()) {
                        AABB collisionAABB = collisionAABBItr.next();
                        if (collisionAABB.intersects(startMin, endMin) || collisionAABB.intersects(startMax, endMax)) {
                            maxY = (float) collisionAABB.maxY;
                            Vec3 collisionAABBCenter = collisionAABB.getCenter();
                            BlockPos centerPos = BlockPos.containing(collisionAABBCenter);

                            for (int i = 1; i < maxHeight; i++) {
                                BlockPos checkingPos = centerPos.above(i);
                                BlockState checkingState = entity.level().getBlockState(checkingPos);
                                VoxelShape checkingShape;
                                if (!(checkingShape = checkingState.getCollisionShape(entity.level(), checkingPos, collisionContext)).isEmpty()) {
                                    maxY = (float) checkingShape.max(Direction.Axis.Y) + checkingPos.getY();
                                    if (maxY - entity.getY() > maxHeight) {
                                        return;
                                    }
                                }

                                if (i > 1) {
                                    maxPos = maxPos.above();
                                    BlockState aboveState2 = entity.level().getBlockState(maxPos);
                                    if (!aboveState2.getCollisionShape(entity.level(), maxPos, collisionContext).isEmpty()) {
                                        return;
                                    }
                                }
                            }
                            break;
                        }
                    }

                    if (maxY != Float.MIN_VALUE) {
                        float deltaY = (float) (maxY - entity.getY());
                        if (!(deltaY <= 0.5F) && !(deltaY > maxHeight)) {
                            autoJumpTimeSetter.run();
                        }
                    }
                }
            }
        }
    }
}
