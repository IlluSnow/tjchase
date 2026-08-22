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

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.MoveControl;

import java.util.function.BooleanSupplier;

public class WrappedMoveControl extends MoveControl {
    private final MoveControl wrapped;
    private final BooleanSupplier active;

    public WrappedMoveControl(Mob mob, BooleanSupplier active) {
        this(mob, mob.getMoveControl(), active);
    }

    public WrappedMoveControl(Mob mob, MoveControl moveControl, BooleanSupplier active) {
        super(mob);
        this.wrapped = moveControl;
        this.active = active;
    }

    @Override
    public boolean hasWanted() {
        return active.getAsBoolean() && wrapped.hasWanted();
    }

    @Override
    public double getSpeedModifier() {
        return wrapped.getSpeedModifier();
    }

    @Override
    public void setWantedPosition(double x, double y, double z, double speed) {
        if (active.getAsBoolean()) {
            wrapped.setWantedPosition(x, y, z, speed);
        }
    }

    @Override
    public void strafe(float forward, float strafe) {
        if (active.getAsBoolean()) {
            wrapped.strafe(forward, strafe);
        }
    }

    @Override
    public void tick() {
        if (active.getAsBoolean()) {
            wrapped.tick();
        }
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
    public void setWait() {
        if (active.getAsBoolean()) {
            wrapped.setWait();
        }
    }
}
