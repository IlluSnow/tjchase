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
import illusnow.tjchase.entity.controllable.Controllable;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public record UpdateControlledEntityPayload(int id, boolean start) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<UpdateControlledEntityPayload> TYPE = new CustomPacketPayload.Type<>(TJChase.prefix("update_controlled_entity"));
    public static final StreamCodec<ByteBuf, UpdateControlledEntityPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, UpdateControlledEntityPayload::id,
            ByteBufCodecs.BOOL, UpdateControlledEntityPayload::start,
            UpdateControlledEntityPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Nullable
    public Controllable getEntity(Level level) {
        return level.getEntity(id) instanceof Controllable controllable ? controllable : null;
    }
}
