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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.slf4j.Logger;

public final class ModServerPayloadHandlers {
    private static final Logger LOGGER = LogUtils.getLogger();

    private ModServerPayloadHandlers() {}

    public static void handleUpdateInput(UpdateInputPayload payload, IPayloadContext context) {
        Player player = context.player();
        Controllable controllingMob = payload.getControllingMob(player.level());
        if (controllingMob == null) {
            warnNotFound(payload.controllingMobId());
            return;
        }
        ControllableMovementHandler.updateControlledMobMovement(controllingMob, player, payload.input());
    }

    public static void handleUpdateControlledEntityRotation(UpdateControlledEntityRotationPayload payload, IPayloadContext context) {
        Controllable controlledEntity = payload.getEntity(context.player().level());
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

    private static void warnNotFound(int id) {
        LOGGER.warn("Unable to find controllingMob with id {}", id);
    }
}
