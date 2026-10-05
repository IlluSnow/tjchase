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

import com.mojang.serialization.Codec;
import illusnow.tjchase.util.ModRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public abstract class StruggleType {
    public static final Codec<StruggleType> CODEC = ModRegistries.STRUGGLE_TYPES.byNameCodec();
    public static final StreamCodec<RegistryFriendlyByteBuf, StruggleType> STREAM_CODEC = ByteBufCodecs.registry(ModRegistries.STRUGGLE_TYPES_KEY);

    public static final int FREQUENCY = 5;
    public static final int INTERVAL = 20 / FREQUENCY;

    private final ItemStack icon;
    private final int maxStruggle;

    protected StruggleType(ItemStack icon, int maxStruggle) {
        this.icon = icon;
        this.maxStruggle = maxStruggle;
    }

    public abstract boolean shouldRemove(Player player);

    public abstract void onSuccessfullyStruggled(Player player);

    public ItemStack getIcon() {
        return icon;
    }

    public int getMaxStruggle() {
        return maxStruggle;
    }
}
