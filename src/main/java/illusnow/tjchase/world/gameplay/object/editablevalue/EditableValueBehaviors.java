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
import illusnow.tjchase.TJChase;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.neoforged.neoforge.common.TranslatableEnum;

import java.util.List;

public final class EditableValueBehaviors {
    private EditableValueBehaviors() {}

    public static RangedBehavior<Integer> ofInt(int min, int max) {
        return new RangedBehavior<>(Codec.intRange(min, max), ByteBufCodecs.INT, min, max, false);
    }

    public static RangedBehavior<Double> ofDouble(double min, double max) {
        return new RangedBehavior<>(Codec.doubleRange(min, max), ByteBufCodecs.DOUBLE, min, max, true);
    }

    public static <E extends Enum<E> & StringRepresentable & TranslatableEnum> CyclingEnumBehavior<E> ofEnum(E[] values, Codec<E> codec, StreamCodec<? super RegistryFriendlyByteBuf, E> streamCodec) {
        return new CyclingEnumBehavior<>(values, codec, streamCodec);
    }

    public record RangedBehavior<N extends Number & Comparable<N>>(Codec<N> codec, StreamCodec<? super RegistryFriendlyByteBuf, N> streamCodec, N min, N max, boolean decimal) implements EditableValueBehavior<N> {
        public static final String HINT_KEY = TJChase.prefix("editable_value_behavior", "ranged.hint");
        public static final String HINT_KEY_DECIMAL = TJChase.prefix("editable_value_behavior", "ranged.hint_decimal");

        public RangedBehavior {
            if (min.compareTo(max) > 0) {
                throw new IllegalArgumentException("min must be less than or equal to max");
            }
        }

        @Override
        public N sanitize(N value) {
            if (value.compareTo(min) < 0) {
                return min;
            }
            if (value.compareTo(max) > 0) {
                return max;
            }
            return value;
        }

        @Override
        public MutableComponent createValueTranslation(N value) {
            return Component.literal(decimal ? "%.2f".formatted(value.doubleValue()) : String.valueOf(value)).withStyle(ChatFormatting.BLUE);
        }

        @Override
        public Pair<N, N> getMinMax() {
            return Pair.of(min, max);
        }

        @Override
        public Component createHint() {
            return Component.translatable(decimal ? HINT_KEY_DECIMAL : HINT_KEY, min, max);
        }
    }

    public static class DefaultBooleanBehavior implements EditableValueBehavior<Boolean> {
        @Override
        public Codec<Boolean> codec() {
            return Codec.BOOL;
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, Boolean> streamCodec() {
            return ByteBufCodecs.BOOL;
        }

        @Override
        public MutableComponent createValueTranslation(Boolean value) {
            return (value ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF).copy().withStyle(ChatFormatting.BLUE);
        }

        @Override
        public List<Boolean> getPossibleValues() {
            return List.of(false, true);
        }
    }

    public record CyclingEnumBehavior<E extends Enum<E> & StringRepresentable & TranslatableEnum>(E[] values, Codec<E> codec, StreamCodec<? super RegistryFriendlyByteBuf, E> streamCodec) implements EditableValueBehavior<E> {
        @Override
        public MutableComponent createValueTranslation(E value) {
            return value.getTranslatedName().copy().withStyle(ChatFormatting.BLUE);
        }

        @Override
        public List<E> getPossibleValues() {
            return List.of(values);
        }
    }
}
