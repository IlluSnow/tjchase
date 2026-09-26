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

package illusnow.tjchase.network.s2c;

import illusnow.tjchase.TJChase;
import illusnow.tjchase.util.Utils;
import illusnow.tjchase.world.gameplay.action.Action;
import illusnow.tjchase.world.gameplay.action.ActionHolder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.players.NameAndId;

import java.util.Optional;

public record UpdateActionPayload(ActionHolder.NetworkOp op, NameAndId player, Optional<Action> newAction, Optional<Action> oldAction) implements CustomPacketPayload {
    public static final Type<UpdateActionPayload> TYPE = new Type<>(TJChase.prefix("update_action"));
    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateActionPayload> STREAM_CODEC = StreamCodec.composite(
            ActionHolder.NetworkOp.STREAM_CODEC, UpdateActionPayload::op,
            Utils.NAME_AND_ID_STREAM_CODEC, UpdateActionPayload::player,
            ByteBufCodecs.optional(Action.STREAM_CODEC), UpdateActionPayload::newAction,
            ByteBufCodecs.optional(Action.STREAM_CODEC), UpdateActionPayload::oldAction,
            UpdateActionPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
