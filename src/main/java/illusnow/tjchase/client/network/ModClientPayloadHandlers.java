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

package illusnow.tjchase.client.network;

import com.mojang.logging.LogUtils;
import illusnow.tjchase.client.gui.screen.GameplayObjectEditScreen;
import illusnow.tjchase.client.resources.sounds.DanceTimeSoundInstance;
import illusnow.tjchase.client.resources.sounds.PrimedRocketSoundInstance;
import illusnow.tjchase.entity.controllable.Controllable;
import illusnow.tjchase.entity.gameplay.InGamePlacedEntity;
import illusnow.tjchase.entity.gameplay.Rocket;
import illusnow.tjchase.entity.gameplay.TyingHelper;
import illusnow.tjchase.network.s2c.*;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.util.function.Function;

public final class ModClientPayloadHandlers {
    private static final Logger LOGGER = LogUtils.getLogger();

    private ModClientPayloadHandlers() {}

    public static void handlePlayDanceTime(PlayDanceTimePayload payload, IPayloadContext context) {
        playSound(payload::getZuri, DanceTimeSoundInstance::new);
    }

    public static void handlePlayFuseSound(PlayFuseSoundPayload payload, IPayloadContext context) {
        playSound(payload::getRocket, PrimedRocketSoundInstance::new);
    }

    private static <T extends Entity> void playSound(EntityGetter<? extends T> entityGetter, Function<? super T, ? extends AbstractSoundInstance> soundFactory) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null) {
            T entity = entityGetter.getEntity(minecraft.level);
            if (entity != null) {
                if (!entity.isSilent()) {
                    SoundEngine.PlayResult playResult = minecraft.getSoundManager().play(soundFactory.apply(entity));
                    if (playResult != SoundEngine.PlayResult.STARTED) {
                        LOGGER.warn("{} was found, but the music was not played normally: {}", I18n.get(entity.getName().getString()), playResult);
                    }
                }
            }
        }
    }

    @FunctionalInterface
    private interface EntityGetter<T extends Entity> {
        @Nullable
        T getEntity(ClientLevel level);
    }

    public static void handleUpdateControlledEntity(UpdateControlledEntityPayload payload, IPayloadContext context) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null) {
            Controllable controllable = payload.getEntity(minecraft.level);
            if (controllable != null && payload.start()) {
                minecraft.setCameraEntity(controllable.getSelfAsEntity());
            }
            if (!payload.start()) {
                minecraft.setCameraEntity(context.player());
            }
        }
    }

    public static void handleOpenGameObjectEditScreen(OpenGameplayObjectEditScreenForInGameEntityPayload payload, IPayloadContext context) {
        InGamePlacedEntity<?> entity = payload.getEntity(context.player().level());
        if (entity == null) {
            warnNotFound(payload.entityId());
            return;
        }
        Minecraft.getInstance().setScreen(new GameplayObjectEditScreen<>(entity.getDisplayName().copy(), entity, entity.createTemplate()));
    }

    public static void handleTiePlayer(TiePlayerPayload payload, IPayloadContext context) {
        LOGGER.debug("Handling tying {}, tie = {}", context.player().getDisplayName().getString(), payload.tie());
        handleTiedPlayerCameraEntity(payload.tie(), context.player());
    }

    @Nullable
    private static CameraType prevCameraType = null;

    public static void handleTiedPlayerCameraEntity(boolean tie, Player player) {
        Minecraft minecraft = Minecraft.getInstance();
        Entity tiedTo = TyingHelper.getTiedTo(player);
        if (tie) {
            if (tiedTo != null && (minecraft.getCameraEntity() == null || tiedTo.getId() != minecraft.getCameraEntity().getId())) {
                if (prevCameraType == null) {
                    prevCameraType = minecraft.options.getCameraType();
                }
                if (tiedTo instanceof Rocket) {
                    minecraft.options.setCameraType(CameraType.FIRST_PERSON);
                }
                if (tiedTo instanceof Player) {
                    minecraft.options.setCameraType(CameraType.THIRD_PERSON_BACK);
                }
                minecraft.setCameraEntity(tiedTo);
            }
        } else {
            minecraft.setCameraEntity(null);
            if (prevCameraType == null) {
                LOGGER.warn("Resetting player's camera type for untied player but prevCameraType was not found, using default");
                prevCameraType = CameraType.FIRST_PERSON;
            }
            minecraft.options.setCameraType(prevCameraType);
            prevCameraType = null;
        }
    }

    public static void handleUpdateAction(UpdateActionPayload payload, IPayloadContext context) {
        Player player = context.player().level().getPlayerByUUID(payload.player().id());
        if (player == null) {
            infoNullPlayer(payload.player());
            return;
        }
        payload.op().handle(player, payload.newAction().orElse(null), payload.oldAction().orElse(null));
    }

    public static void handleUpdateWeakStatus(UpdateWeakStatusPayload payload, IPayloadContext context) {
        Player player = context.player().level().getPlayerByUUID(payload.player());
        if (player == null) {
            infoNullPlayer(payload.player());
            return;
        }
        player.refreshDimensions();
        if (payload.weak() && player.isLocalPlayer()) {
            if (Minecraft.getInstance().screen instanceof AbstractContainerScreen<?>) {
                Minecraft.getInstance().setScreen(null);
            }
        }
    }

    private static void infoNullPlayer(Object id) {
        LOGGER.info("Player with {} {} not found, ignored", id.getClass().getSimpleName(), id);
    }

    private static void warnNotFound(int id) {
        LOGGER.warn("Unable to find entity with id {}", id);
    }
}
