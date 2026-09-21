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
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public record UpdateInputPayload(int controllingMobId, Input input) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<UpdateInputPayload> TYPE = new CustomPacketPayload.Type<>(TJChase.prefix("update_input"));
    public static final StreamCodec<FriendlyByteBuf, UpdateInputPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, UpdateInputPayload::controllingMobId,
            Input.STREAM_CODEC, UpdateInputPayload::input,
            UpdateInputPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Nullable
    public Controllable getControllingMob(Level level, Player player) {
        return level.getEntity(controllingMobId) instanceof Controllable controllable && controllable.getPlayerController() == player ? controllable : null;
    }
}
