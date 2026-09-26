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
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.UUID;

public record UpdateWeakStatusPayload(UUID player, boolean weak) implements CustomPacketPayload {
    public static final Type<UpdateWeakStatusPayload> TYPE = new Type<>(TJChase.prefix("update_weak_status"));
    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateWeakStatusPayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, UpdateWeakStatusPayload::player,
            ByteBufCodecs.BOOL, UpdateWeakStatusPayload::weak,
            UpdateWeakStatusPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
