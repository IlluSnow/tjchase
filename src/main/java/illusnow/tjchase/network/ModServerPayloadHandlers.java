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

package illusnow.tjchase.network;

import com.mojang.logging.LogUtils;
import illusnow.tjchase.entity.controllable.Controllable;
import illusnow.tjchase.entity.controllable.ControllableMovementHandler;
import illusnow.tjchase.entity.gameplay.InGamePlacedEntity;
import illusnow.tjchase.item.ModDataComponents;
import illusnow.tjchase.network.bidirectional.SyncInGameEntityPayload;
import illusnow.tjchase.network.c2s.LoadTemplatePayload;
import illusnow.tjchase.network.c2s.UpdateControlledEntityPositionPayload;
import illusnow.tjchase.network.c2s.UpdateControlledEntityRotationPayload;
import illusnow.tjchase.network.c2s.UpdateInputPayload;
import illusnow.tjchase.world.gameplay.object.Template;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.slf4j.Logger;

public final class ModServerPayloadHandlers {
    private static final Logger LOGGER = LogUtils.getLogger();

    private ModServerPayloadHandlers() {}

    public static void handleUpdateInput(UpdateInputPayload payload, IPayloadContext context) {
        Player player = context.player();
        Controllable controllingMob = payload.getControllingMob(player.level(), player);
        if (controllingMob == null) {
            warnNotFound(payload.controllingMobId());
            return;
        }
        ControllableMovementHandler.updateControlledMobMovement(controllingMob, player, payload.input());
    }

    public static void handleUpdateControlledEntityRotation(UpdateControlledEntityRotationPayload payload, IPayloadContext context) {
        Controllable controlledEntity = payload.getEntity(context.player().level(), context.player());
        if (controlledEntity == null) {
            warnNotFound(payload.id());
            return;
        }
        LivingEntity living = controlledEntity.getSelfAsEntity();
        living.setYRot(payload.yRot());
        living.yHeadRot = payload.yHeadRot();
        living.yBodyRot = payload.yBodyRot();
        living.setXRot(payload.xRot());

        living.yRotO = payload.yRot();
        living.xRotO = payload.xRot();
        living.yHeadRotO = payload.yHeadRot();
        living.yBodyRotO = payload.yBodyRot();
    }

    public static void handleUpdateControlledEntityPosition(UpdateControlledEntityPositionPayload payload, IPayloadContext context) {
        Controllable controlledEntity = payload.getEntity(context.player().level(), context.player());
        if (controlledEntity == null) {
            warnNotFound(payload.id());
            return;
        }
        LivingEntity living = controlledEntity.getSelfAsEntity();
        living.snapTo(payload.position());
    }

    @SuppressWarnings("unchecked")
    public static <O extends InGamePlacedEntity<O>> void handleSyncInGameEntity(SyncInGameEntityPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            InGamePlacedEntity<O> entity = (InGamePlacedEntity<O>) payload.getEntity(context.player().level());
            if (entity == null) {
                warnNotFound(payload.entityId());
                return;
            }
            Template<O> template = (Template<O>) payload.template();
            entity.loadFromTemplate(template);
            if (!payload.name().isEmpty()) {
                entity.setCustomName(Component.literal(payload.name()));
            }
        }).exceptionally(throwable -> {
            LOGGER.error("Failed to sync InGameEntity", throwable);
            //noinspection DataFlowIssue
            return null;
        });
    }

    public static void handleLoadTemplate(LoadTemplatePayload payload, IPayloadContext context) {
        ItemStack mainHandItem = context.player().getMainHandItem();
        mainHandItem.set(ModDataComponents.PLACE_TEMPLATE, payload.template().isDefault() ? null : payload.template());
    }

    private static void warnNotFound(int id) {
        LOGGER.warn("Unable to find entity with id {}", id);
    }
}
