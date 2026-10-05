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

package illusnow.tjchase.world.gameplay.struggle;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import illusnow.tjchase.attachment.ModAttachments;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Optional;

public class StruggleInstance {
    public static final Codec<StruggleInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            StruggleType.CODEC.fieldOf("type").forGetter(StruggleInstance::getType),
            Codec.LONG.fieldOf("last_update_game_time").forGetter(StruggleInstance::getLastUpdateGameTime),
            Codec.INT.fieldOf("current_struggle").forGetter(StruggleInstance::getCurrentStruggle)
    ).apply(instance, StruggleInstance::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, StruggleInstance> STREAM_CODEC = StreamCodec.composite(
            StruggleType.STREAM_CODEC, StruggleInstance::getType,
            ByteBufCodecs.INT, StruggleInstance::getCurrentStruggle,
            StruggleInstance::new
    );
    private static final Logger LOGGER = LogUtils.getLogger();

    private final StruggleType type;
    // Server-side update only variable
    private long lastUpdateGameTime;
    private int currentStruggle;

    public StruggleInstance(StruggleType type, int currentStruggle) {
        this(type, 0, currentStruggle);
    }

    public StruggleInstance(StruggleType type, long lastUpdateGameTime, int currentStruggle) {
        this.type = type;
        this.lastUpdateGameTime = lastUpdateGameTime;
        this.currentStruggle = currentStruggle;
    }

    @Nullable
    public static StruggleInstance getStruggle(Player player) {
        return player.getData(ModAttachments.STRUGGLE).orElse(null);
    }

    public static void setStruggle(Player player, @Nullable StruggleInstance struggle) {
        player.setData(ModAttachments.STRUGGLE, Optional.ofNullable(struggle));
    }

    public static StruggleInstance createNew(StruggleType type) {
        return new StruggleInstance(type, 0);
    }

    public StruggleType getType() {
        return type;
    }

    public long getLastUpdateGameTime() {
        return lastUpdateGameTime;
    }

    public int getCurrentStruggle() {
        return currentStruggle;
    }

    public static void updateBidirectionally(Player player) {
        StruggleInstance struggle = getStruggle(player);
        if (struggle == null) {
            return;
        }
        if (!player.level().isClientSide()) {
            boolean shouldRemove = struggle.checkValidity(player);
            if (shouldRemove) {
                setStruggle(player, null);
            }
        }
    }

    private boolean checkValidity(Player player) {
        return type.shouldRemove(player);
    }

    public double getPercentage() {
        return (double) currentStruggle / type.getMaxStruggle();
    }

    public boolean clientStruggle(Player player) {
        if (!player.level().isClientSide()) {
            throw new IllegalStateException("clientStruggle should only be called on the client side");
        }
        return true;
    }

    public boolean serverStruggle(Player player) {
        if (player.level().isClientSide()) {
            throw new IllegalStateException("serverStruggle should only be called on the server side");
        }
        if (player.level().getGameTime() - lastUpdateGameTime < StruggleType.INTERVAL) {
            return false;
        }
        lastUpdateGameTime = player.level().getGameTime();
        currentStruggle++;
        if (currentStruggle >= type.getMaxStruggle()) {
            type.onSuccessfullyStruggled(player);
            return true;
        } else {
            return false;
        }
    }
}
