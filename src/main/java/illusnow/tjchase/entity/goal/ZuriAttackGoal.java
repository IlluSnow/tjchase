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

package illusnow.tjchase.entity.goal;

import illusnow.tjchase.entity.Zuri;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class ZuriAttackGoal extends Goal {
    private final Zuri zuri;
    private final double speedModifier;
    private final double retreatSpeedModifier;
    private final float attackRadiusSqr;
    private final float retreatRadius;
    private final float retreatRadiusSqr;
    private final float safeRadius;
    private final int retreatInterval;
    private int attackTime = -1;
    private int seeTime;
    private boolean strafingClockwise;
    private boolean strafingBackwards;
    private int strafingTime = -1;
    private boolean retreating;
    private int retreatTime = 0;

    public ZuriAttackGoal(Zuri zuri, double speedModifier, double retreatSpeedModifier, float attackRadius, float retreatRadius, int retreatInterval) {
        this.zuri = zuri;
        this.speedModifier = speedModifier;
        this.retreatSpeedModifier = retreatSpeedModifier;
        this.attackRadiusSqr = attackRadius * attackRadius;
        this.retreatRadius = retreatRadius;
        this.retreatRadiusSqr = retreatRadius * retreatRadius;
        this.safeRadius = (attackRadius + retreatRadius) / 2;
        this.retreatInterval = retreatInterval;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return zuri.getTarget() != null;
    }

    @Override
    public boolean canContinueToUse() {
        return canUse() || !zuri.getNavigation().isDone();
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void stop() {
        super.stop();
        seeTime = 0;
        attackTime = (zuri.getYogaBallAttackInterval() + zuri.getMeleeAttackInterval()) / 4;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = zuri.getTarget();
        int maxRetreatTime = 80;
        int retreatCD = 200;

        if (retreating || retreatTime < 0) {
            retreatTime++;
        }
        if (zuri.isDancing()) {
            zuri.getNavigation().stop();
        }

        if (target == null) {
            retreating = false;
            retreatTime = retreatCD;
        } else  {
            double distSqr = zuri.distanceToSqr(target.getX(), target.getY(), target.getZ());
            boolean canSee = zuri.getSensing().hasLineOfSight(target);
            boolean seen = seeTime > 0;
            if (canSee != seen) {
                seeTime = 0;
            }
            if (canSee) {
                seeTime++;
            } else {
                seeTime--;
            }

            if (retreating) {
                if (retreatTime >= maxRetreatTime || distSqr >= safeRadius * safeRadius || zuri.getNavigation().isDone()) {
                    retreating = false;
                    retreatTime = -retreatCD;
                    attackTime = Math.min(attackTime, zuri.getYogaBallAttackInterval() / 3);
                }
                return;
            }

            attackTime--;

            if (!zuri.isDancing()) {
                if (distSqr <= attackRadiusSqr && seeTime >= 5) {
                    Path path = null;
                    if (retreatTime == 0 && zuri.getRandom().nextInt(retreatInterval) == 0 && zuri.getHealth() < zuri.getMaxHealth() * 0.5F) {
                        for (Mob mob : zuri.level().getEntitiesOfClass(Mob.class, zuri.getBoundingBox().inflate(retreatRadius), mob -> mob != zuri && mob.distanceToSqr(zuri) <= retreatRadiusSqr && mob.getTarget() == zuri)) {
                            path = getRetreatPath(mob);
                            if (path != null) {
                                break;
                            }
                        }
                    }
                    if (path != null) {
                        zuri.getNavigation().moveTo(path, retreatSpeedModifier);
                        retreating = true;
                    } else {
                        zuri.getNavigation().stop();
                        strafingTime++;
                    }
                } else {
                    zuri.getNavigation().moveTo(target, speedModifier);
                    strafingTime = -1;
                }

                if (strafingTime >= 20) {
                    if (zuri.getRandom().nextFloat() < 0.3) {
                        strafingClockwise = !strafingClockwise;
                    }

                    if (zuri.getRandom().nextFloat() < 0.3) {
                        strafingBackwards = !strafingBackwards;
                    }

                    strafingTime = 0;
                }

                if (strafingTime > -1) {
                    if (distSqr > attackRadiusSqr * 0.75F) {
                        strafingBackwards = false;
                    } else if (distSqr < attackRadiusSqr * 0.25F) {
                        strafingBackwards = true;
                    }

                    zuri.getMoveControl().strafe(strafingBackwards ? -0.5F : 0.5F, strafingClockwise ? 0.5F : -0.5F);
                    if (zuri.getControlledVehicle() instanceof Mob mob) {
                        mob.lookAt(target, 60, 60);
                    }

                    zuri.lookAt(target, 60, 60);
                } else {
                    zuri.getLookControl().setLookAt(target, 60, 60);
                }
            }

            double meleeAttackRange = zuri.getMeleeAttackRange();
            boolean withinMeleeAttackRange = distSqr <= meleeAttackRange * meleeAttackRange;
            boolean performedMeleeAttack = false;
            if (attackTime <= 0 && seeTime >= -60) {
                if (withinMeleeAttackRange) {
                    if (zuri.meleeAttack(target, meleeAttackRange)) {
                        attackTime = zuri.getMeleeAttackInterval();
                        retreatTime = -20;
                        performedMeleeAttack = true;
                    }
                }
                if (!performedMeleeAttack) {
                    zuri.performRangedAttack(target, 0);
                    attackTime = zuri.getYogaBallAttackInterval();
                }
            }
        }
    }

    @Nullable
    private Path getRetreatPath(LivingEntity target) {
        Vec3 vec3 = null;
        for (int i = 0; i < 5; i++) {
            vec3 = DefaultRandomPos.getPosAway(zuri, (int) safeRadius, 7, target.position());
            if (vec3 != null && target.distanceToSqr(vec3.x, vec3.y, vec3.z) >= target.distanceToSqr(zuri)) {
                break;
            }
        }
        if (vec3 == null) {
            return null;
        } else if (target.distanceToSqr(vec3.x, vec3.y, vec3.z) < target.distanceToSqr(zuri)) {
            return null;
        } else {
            return zuri.getNavigation().createPath(vec3.x, vec3.y, vec3.z, 0);
        }
    }
}
