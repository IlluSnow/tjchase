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
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ClaimEnvelopeRewardPayload(boolean offHand) implements CustomPacketPayload {
    public static final Type<ClaimEnvelopeRewardPayload> TYPE = new Type<>(TJChase.prefix("claim_envelope_reward"));
    public static final StreamCodec<ByteBuf, ClaimEnvelopeRewardPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, ClaimEnvelopeRewardPayload::offHand,
            ClaimEnvelopeRewardPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
