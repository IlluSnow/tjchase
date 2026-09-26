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

import com.mojang.logging.LogUtils;
import illusnow.tjchase.attachment.ModAttachments;
import illusnow.tjchase.entity.ModEntities;
import illusnow.tjchase.network.s2c.TiePlayerPayload;
import illusnow.tjchase.world.gameplay.action.ActionHolder;
import illusnow.tjchase.world.gameplay.action.ModActions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class TyingHelper {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<EntityType<?>, PositionCalculator<Entity>> POSITION_CALCULATORS = new HashMap<>();

    static {
        registerTyingOffsetCalculator(ModEntities.ROCKET.get(), (rocket, tying) ->
                rocket.position().add(0, 1.6 - tying.getBbHeight() / 2, 0).add(rocket.getLookAngle().scale(0.52)));
        registerTyingOffsetCalculator(EntityType.PLAYER, (player, tying) -> {
                float yBodyRot = player.yBodyRot + 90;
                double dx = Mth.cos(yBodyRot * Math.PI / 180);
                double dz = Mth.sin(yBodyRot * Math.PI / 180);
                double relativeWidth = tying.getBbWidth() / player.getBbWidth();
                return player.position().add(0, player.getBbHeight() / 3, 0).add(dx * (0.3 + relativeWidth * 0.5), 0, dz * (0.3 + relativeWidth * 0.5));
        });
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

    public static boolean canTiePlayer(Entity entity) {
        return POSITION_CALCULATORS.containsKey(entity.getType());
    }

    public static boolean isMovementRestricted(Player player) {
        return getTiedTo(player) != null;
    }

    public static boolean isCameraRestricted(Player player) {
        return getTiedTo(player) != null;
    }

    public static void tie(@Nullable Player player, @Nullable Entity tiedTo) {
        if (player == null && tiedTo == null) {
            return;
        }
        Entity prevTiedTo;
        Player prevTying;
        if (player != null) {
            prevTiedTo = getTiedTo(player);
            if (prevTiedTo != tiedTo) {
                if (prevTiedTo != null) {
                    prevTiedTo.setData(ModAttachments.TYING, Optional.empty());
                }
                player.setData(ModAttachments.TIED_TO, Optional.ofNullable(EntityReference.of(tiedTo)));
            }
        }
        if (tiedTo != null) {
            prevTying = getTying(tiedTo);
            if (prevTying != player) {
                if (prevTying != null) {
                    prevTying.setData(ModAttachments.TIED_TO, Optional.empty());
                    updatePlayer(prevTying, null);
                }
                tiedTo.setData(ModAttachments.TYING, Optional.ofNullable(EntityReference.of(player)));
            }
        }

        if (player != null) {
            updatePlayer(player, tiedTo);
        }
    }

    private static void updatePlayer(Player player, @Nullable Entity tiedTo) {
        if (tiedTo != null) {
            restrictTiedPlayerMovement(player, tiedTo, true);
            player.setNoGravity(true);
            player.getData(ModAttachments.WEAK_STATE).recoverFromWeak(player);
        } else {
            player.setNoGravity(false);
        }
        LOGGER.debug("Sending tying packet to {}, tie = {}", player.getDisplayName().getString(), tiedTo != null);
        PacketDistributor.sendToPlayer((ServerPlayer) player, new TiePlayerPayload(tiedTo != null));
    }

    public static void clearIfInvalid(Entity entity) {
        if (entity instanceof Player player) {
            Entity prevTiedTo = getTiedTo(player);
            if (prevTiedTo != null && (!prevTiedTo.isAlive() || prevTiedTo.isSpectator())) {
                clearTying(player, prevTiedTo);
                clearStruggleOrPrayAction(player);
                if (prevTiedTo instanceof Player prevPlayerTiedTo){
                    clearHugOrTieAction(prevPlayerTiedTo);
                }
            } else if (prevTiedTo == null && player.getData(ModAttachments.TIED_TO).isPresent()) {
                tie(player, null);
                clearStruggleOrPrayAction(player);
            }
        }
        Player prevTying = getTying(entity);
        if (prevTying != null && (!prevTying.isAlive() || prevTying.isSpectator())) {
            clearTying(prevTying, entity);
            clearStruggleOrPrayAction(prevTying);
            if (entity instanceof Player player) {
                clearHugOrTieAction(player);
            }
        } else if (prevTying == null && entity.getData(ModAttachments.TYING).isPresent()) {
            tie(null, entity);
            if (entity instanceof Player player) {
                clearHugOrTieAction(player);
            }
        }
    }

    public static boolean clearStruggleOrPrayAction(Player playerBeingTied) {
        return ActionHolder.stopActionIf(playerBeingTied, action -> action == ModActions.STRUGGLE.get() || action == ModActions.PRAY.get());
    }

    public static boolean clearHugOrTieAction(Player playerHuggingOrTying) {
        return ActionHolder.stopActionIf(playerHuggingOrTying, action -> action == ModActions.HUG.get() || action == ModActions.TIE.get());
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

    public static void clearTying(@Nullable Player player, @Nullable Entity tiedTo) {
        if (player != null && player.isAlive()) {
            tie(player, null);
        }
        if (tiedTo != null && tiedTo.isAlive()) {
            tie(null, tiedTo);
        }
    }

    public static void restrictTiedPlayerMovement(Player player, Entity tiedTo, boolean firstUpdate) {
        if (firstUpdate) {
            player.snapTo(getTyingPosition(tiedTo, player), tiedTo.getYRot(), 0);
        } else {
            player.setPos(getTyingPosition(tiedTo, player));
            player.setYRot(tiedTo.getYRot());
            player.setXRot(0);
            player.yRotO = tiedTo.yRotO;
        }
        player.setDeltaMovement(Vec3.ZERO);
        if (tiedTo instanceof LivingEntity living) {
            player.setYBodyRot(living.yBodyRot);
            player.setYHeadRot(living.yBodyRot);
        } else {
            player.setYHeadRot(tiedTo.getYRot());
            player.setYBodyRot(tiedTo.getYRot());
        }
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.resetPosition();
        }
    }

    public interface PositionCalculator<T extends Entity> {
        Vec3 calculate(T entity, Player tying);
    }
}
