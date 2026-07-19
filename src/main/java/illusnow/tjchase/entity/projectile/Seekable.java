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

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;

public interface Seekable {
    private Projectile self() {
        return (Projectile) this;
    }

    double getSeekPower();

    default boolean canSeek() {
        return getSeekPower() > 0;
    }

    default void trySeek(Entity target) {
        Vec3 deltaMovement = self().getDeltaMovement();
        double dx = target.getX() - self().getX();
        double dy = target.getY(0.5) - self().getY(0.5);
        double dz = target.getZ() - self().getZ();
        double movementLen = deltaMovement.length();
        Vec3 vecToTarget = new Vec3(dx, dy, dz).normalize().scale(movementLen);
        double seekPower = Mth.clamp(getSeekPower() * movementLen / 2, 0, 1);
        Vec3 newDirection = deltaMovement.scale(1 - seekPower).add(vecToTarget.scale(seekPower));
        self().setDeltaMovement(newDirection.normalize().scale(movementLen));
    }
}
