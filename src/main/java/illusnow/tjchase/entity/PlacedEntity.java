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

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.InterpolationHandler;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

public abstract class PlacedEntity extends Entity {
    private final InterpolationHandler interpolation = new InterpolationHandler(this, 3);

    protected PlacedEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource damageSource, float amount) {
        if (isInvulnerableToBase(damageSource)) {
            return false;
        }
        onHit(level, damageSource, amount);
        markHurt();
        gameEvent(GameEvent.ENTITY_DAMAGE, damageSource.getEntity());
        return true;
    }

    protected void onHit(ServerLevel level, DamageSource damageSource, float amount) {}

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide()) {
            handleMovements();
        }
        interpolation.interpolate();
    }

    @Override
    public InterpolationHandler getInterpolation() {
        return interpolation;
    }

    protected void handleMovements() {
        applyGravity();
        applyInertia();
        move(MoverType.SELF, getDeltaMovement());
        applyEffectsFromBlocks();
        handlePortal();
        if (!onGround() || getDeltaMovement().lengthSqr() > 1E-5) {
            needsSync = true;
        }
    }

    protected void applyInertia() {
        setDeltaMovement(getDeltaMovement().scale(getInertia()));
        if (getDeltaMovement().lengthSqr() <= 1E-6) {
            setDeltaMovement(Vec3.ZERO);
        }
    }

    protected double getInertia() {
        return 0.95;
    }

    @Override
    protected double getDefaultGravity() {
        return 0.08;
    }
}
