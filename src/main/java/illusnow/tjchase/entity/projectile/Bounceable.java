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

import net.minecraft.core.Direction;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public interface Bounceable {
    void updateRotationWhenBouncing();

    static ProjectileDeflection getProjectileDeflection(Direction direction) {
        return switch (direction.getAxis()) {
            case X -> ModProjectileDeflections.BOUNCE_FORWARD_X;
            case Y -> ModProjectileDeflections.BOUNCE_FORWARD_Y;
            case Z -> ModProjectileDeflections.BOUNCE_FORWARD_Z;
        };
    }

    static ProjectileDeflection getProjectileDeflection(Vec3 hitVector, @Nullable EntityHitResult entityHitResult) {
        if (entityHitResult == null) {
            return getProjectileDeflection(Direction.getApproximateNearest(hitVector.x, hitVector.y, hitVector.z));
        }

        Vec3 hitLocation = entityHitResult.getLocation();
        if (hitLocation.equals(entityHitResult.getEntity().position())) {
            // ProjectileUtil's movement trace creates EntityHitResult(entity),
            // whose default location is the entity position, not the impact point.
            return getProjectileDeflection(Direction.getApproximateNearest(hitVector.x, hitVector.y, hitVector.z));
        }

        // ProjectileUtil expands the entity box by 0.3 when tracing projectile
        // impacts, so classify the face against that same box.
        var entityBoundingBox = entityHitResult.getEntity().getBoundingBox().inflate(0.3);
        double distanceToXFace = Math.min(Math.abs(hitLocation.x - entityBoundingBox.minX), Math.abs(hitLocation.x - entityBoundingBox.maxX));
        double distanceToYFace = Math.min(Math.abs(hitLocation.y - entityBoundingBox.minY), Math.abs(hitLocation.y - entityBoundingBox.maxY));
        double distanceToZFace = Math.min(Math.abs(hitLocation.z - entityBoundingBox.minZ), Math.abs(hitLocation.z - entityBoundingBox.maxZ));

        // At edges or corners, prefer the vertical face so top/bottom entity hits
        // consistently bounce on Y regardless of projectile direction.
        if (distanceToYFace <= distanceToXFace && distanceToYFace <= distanceToZFace) {
            return ModProjectileDeflections.BOUNCE_FORWARD_Y;
        }
        if (distanceToXFace <= distanceToZFace) {
            return ModProjectileDeflections.BOUNCE_FORWARD_X;
        }
        return ModProjectileDeflections.BOUNCE_FORWARD_Z;
    }

    static ProjectileDeflection getProjectileDeflection(BlockHitResult result) {
        return getProjectileDeflection(result.getDirection());
    }
}
