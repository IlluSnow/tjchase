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
import illusnow.tjchase.entity.gameplay.Rocket;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public record PlayFuseSoundPayload(int rocketId) implements CustomPacketPayload {
    public static final Type<PlayFuseSoundPayload> TYPE = new Type<>(TJChase.prefix("play_fuse_sound"));
    public static final StreamCodec<ByteBuf, PlayFuseSoundPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, PlayFuseSoundPayload::rocketId,
            PlayFuseSoundPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Nullable
    public Rocket getRocket(Level level) {
        return level.getEntity(rocketId) instanceof Rocket rocket ? rocket : null;
    }
}
