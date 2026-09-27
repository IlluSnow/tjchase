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

package illusnow.tjchase.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record Progress(long startTick, long endTick) {
    public static final Codec<Progress> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.LONG.fieldOf("start_tick").forGetter(Progress::startTick),
            Codec.LONG.fieldOf("end_tick").forGetter(Progress::endTick)
    ).apply(instance, Progress::new));
    public static final StreamCodec<ByteBuf, Progress> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG, Progress::startTick,
            ByteBufCodecs.VAR_LONG, Progress::endTick,
            Progress::new
    );

    public static Progress createWithDuration(long currentTickCount, int duration) {
        return new Progress(currentTickCount, currentTickCount + duration);
    }

    public float getProgress(long tickCount, float partialTick) {
        long activeTicks = tickCount - startTick;
        float activeTicksWithPartialTick = activeTicks + partialTick;
        return Math.clamp(activeTicksWithPartialTick / (endTick - startTick), 0, 1);
    }

    public boolean shouldRemove(long tickCount) {
        return tickCount < startTick || tickCount >= endTick;
    }

}
