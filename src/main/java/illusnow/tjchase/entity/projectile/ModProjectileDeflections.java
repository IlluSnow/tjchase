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

import net.minecraft.world.entity.projectile.ProjectileDeflection;

public final class ModProjectileDeflections {
    public static final ProjectileDeflection YOGA_BALL_BOUNCE_FORWARD_X = (projectile, entity, random) -> {
        projectile.setDeltaMovement(projectile.getDeltaMovement().multiply(-1, 1, 1));
        ((YogaBall) projectile).updateRotationWhenBouncing();
        projectile.needsSync = true;
    };
    public static final ProjectileDeflection YOGA_BALL_BOUNCE_FORWARD_Y = (projectile, entity, random) -> {
        projectile.setDeltaMovement(projectile.getDeltaMovement().multiply(1, -1, 1));
        ((YogaBall) projectile).updateRotationWhenBouncing();
        projectile.needsSync = true;
    };
    public static final ProjectileDeflection YOGA_BALL_BOUNCE_FORWARD_Z = (projectile, entity, random) -> {
        projectile.setDeltaMovement(projectile.getDeltaMovement().multiply(1, 1, -1));
        ((YogaBall) projectile).updateRotationWhenBouncing();
        projectile.needsSync = true;
    };

    private ModProjectileDeflections() {}
}
