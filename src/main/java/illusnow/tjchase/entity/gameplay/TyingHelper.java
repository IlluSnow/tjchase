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

package illusnow.tjchase.entity.gameplay;

import illusnow.tjchase.attachment.ModAttachments;
import illusnow.tjchase.entity.ModEntities;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class TyingHelper {
    private static final Map<EntityType<?>, PositionCalculator<Entity>> POSITION_CALCULATORS = new HashMap<>();

    static {
        registerTyingOffsetCalculator(ModEntities.ROCKET.get(), (rocket, tying) ->
                rocket.position().add(0, 2 - tying.getBbHeight() / 2, 0).add(rocket.getLookAngle().scale(0.7)));
        registerTyingOffsetCalculator(EntityType.PLAYER, (player, tying) -> player.position().add(0, 0.5, 0).add(player.getLookAngle()));
    }

    private TyingHelper() {}

    @Nullable
    public static Player getTying(Entity entity) {
        return entity.getData(ModAttachments.TYING).map(ref -> EntityReference.getPlayer(ref, entity.level())).orElse(null);
    }

    @Nullable
    public static Entity getTiedTo(Player player) {
        return player.getData(ModAttachments.TIED_TO).map(ref -> EntityReference.getEntity(ref, player.level())).orElse(null);
    }

    public static boolean isMovementRestricted(Player player) {
        return getTiedTo(player) != null;
    }

    public static boolean isCameraRestricted(Player player) {
        return getTiedTo(player) != null;
    }

    public static boolean isActionRestricted(Player player) {
        return getTiedTo(player) != null;
    }

    public static void tie(@Nullable Player player, @Nullable Entity tiedTo) {
        if (player != null) {
            player.setData(ModAttachments.TIED_TO, Optional.ofNullable(EntityReference.of(tiedTo)));
        }
        if (tiedTo != null) {
            tiedTo.setData(ModAttachments.TYING, Optional.ofNullable(EntityReference.of(player)));
        }
    }

    public static void clearInvalid(Entity entity) {
        if (entity instanceof Player player) {
            Entity prevTiedTo = getTiedTo(player);
            if (prevTiedTo != null) {
                if (!prevTiedTo.isAlive()) {
                    tie(player, null);
                }
            }
        }
        Entity prevTying = getTying(entity);
        if (prevTying != null) {
            if (!prevTying.isAlive()) {
                tie(null, entity);
            }
        }
    }

    public static Vec3 getTyingPosition(Entity entity, Player tying) {
        PositionCalculator<Entity> calculator = POSITION_CALCULATORS.get(entity.getType());
        Objects.requireNonNull(calculator, "Tying offset calculator for %s is not present".formatted(entity.getType()));
        return calculator.calculate(entity, tying);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Entity> void registerTyingOffsetCalculator(EntityType<T> type, PositionCalculator<? super T> positionCalculator) {
        POSITION_CALCULATORS.put(type, (entity, tying) -> positionCalculator.calculate((T) entity, tying));
    }

    public interface PositionCalculator<T extends Entity> {
        Vec3 calculate(T entity, Player tying);
    }
}
