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

package illusnow.tjchase.network.c2s;

import illusnow.tjchase.TJChase;
import illusnow.tjchase.entity.controllable.Controllable;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public record UpdateControlledEntityPositionPayload(int id, Vec3 position) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<UpdateControlledEntityPositionPayload> TYPE = new CustomPacketPayload.Type<>(TJChase.prefix("update_controlled_entity_position"));
    public static final StreamCodec<ByteBuf, UpdateControlledEntityPositionPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, UpdateControlledEntityPositionPayload::id,
            Vec3.STREAM_CODEC, UpdateControlledEntityPositionPayload::position,
            UpdateControlledEntityPositionPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Nullable
    public Controllable getEntity(Level level, Player player) {
        return level.getEntity(id) instanceof Controllable controllable && controllable.getPlayerController() == player ? controllable : null;
    }
}
