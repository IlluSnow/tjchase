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

import illusnow.tjchase.mixin.LookControlAccessor;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.function.BooleanSupplier;

public class WrappedLookControl extends LookControl {
    private final LookControl wrapped;
    private final BooleanSupplier active;

    public WrappedLookControl(Mob mob, BooleanSupplier active) {
        this(mob, mob.getLookControl(), active);
    }

    public WrappedLookControl(Mob mob, LookControl lookControl, BooleanSupplier active) {
        super(mob);
        this.wrapped = lookControl;
        this.active = active;
    }

    @Override
    public void setLookAt(Vec3 lookVector) {
        wrapped.setLookAt(lookVector);
    }

    @Override
    public void setLookAt(double x, double y, double z, float deltaYaw, float deltaPitch) {
        if (active.getAsBoolean()) {
            wrapped.setLookAt(x, y, z, deltaYaw, deltaPitch);
        }
    }

    @Override
    public void tick() {
        if (active.getAsBoolean()) {
            wrapped.tick();
        }
    }

    @Override
    protected void clampHeadRotationToBody() {
        if (active.getAsBoolean()) {
            ((LookControlAccessor) wrapped).callClampHeadRotationToBody();
        }
    }

    @Override
    protected boolean resetXRotOnTick() {
        return ((LookControlAccessor) wrapped).callResetXRotOnTick();
    }

    @Override
    public boolean isLookingAtTarget() {
        return active.getAsBoolean() && wrapped.isLookingAtTarget();
    }

    @Override
    public double getWantedX() {
        return wrapped.getWantedX();
    }

    @Override
    public double getWantedY() {
        return wrapped.getWantedY();
    }

    @Override
    public double getWantedZ() {
        return wrapped.getWantedZ();
    }

    @Override
    protected Optional<Float> getXRotD() {
        return ((LookControlAccessor) wrapped).callGetXRotD();
    }

    @Override
    protected Optional<Float> getYRotD() {
        return ((LookControlAccessor) wrapped).callGetYRotD();
    }
}
