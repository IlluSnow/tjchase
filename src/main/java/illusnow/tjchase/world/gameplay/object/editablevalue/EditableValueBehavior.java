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

package illusnow.tjchase.world.gameplay.object.editablevalue;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.StreamCodec;
import org.jspecify.annotations.Nullable;

import java.util.List;

public interface EditableValueBehavior<T> {
    Codec<T> codec();

    StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec();

    default T sanitize(T value) {
        return value;
    }

    default MutableComponent createValueTranslation(T value) {
        return Component.literal(value.toString()).withStyle(ChatFormatting.AQUA);
    }

    @Nullable
    default Pair<T, T> getMinMax() {
        return null;
    }

    @Nullable
    default List<T> getPossibleValues() {
        return null;
    }

    @Nullable
    default Component createHint() {
        Pair<T, T> minMax = getMinMax();
        return minMax == null ? null : Component.literal("[%s, %s]".formatted(minMax.getFirst(), minMax.getSecond()));
    }
}
