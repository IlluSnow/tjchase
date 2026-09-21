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

import com.mojang.serialization.Codec;
import illusnow.tjchase.TJChase;
import illusnow.tjchase.util.ModRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

public interface EditableValue<T, O> {
    Codec<EditableValue<?, ?>> CODEC = ModRegistries.EDITABLE_VALUES.byNameCodec();

    static <T, O> Builder<T, O> of(Function<? super O, ? extends T> getter, BiConsumer<? super O, ? super T> setter) {
        return new Builder<>(getter, setter);
    }

    String getName();

    String getTranslationKey();

    default MutableComponent createTranslation() {
        return Component.translatable(getTranslationKey());
    }

    T get(O obj);

    void set(O obj, T value);

    T defaultValue();

    EditableValueBehavior<T> getBehavior();

    default boolean isDefault(@Nullable Object value) {
        return Objects.equals(value, defaultValue());
    }

    default void setDefault(O obj) {
        set(obj, defaultValue());
    }

    default void serialize(ValueOutput output, O obj) {
        output.store(getName(), getBehavior().codec(), get(obj));
    }

    default void deserialize(ValueInput input, O obj) {
        input.read(getName(), getBehavior().codec()).ifPresentOrElse(value -> set(obj, value), () -> set(obj, defaultValue()));
    }

    class Builder<T, O> {
        @Nullable
        private EditableValueBehavior<T> behavior;
        private String id = TJChase.MODID;
        @Nullable
        private T defaultValue;
        private final Function<? super O, ? extends T> getter;
        private final BiConsumer<? super O, ? super T> setter;

        Builder(Function<? super O, ? extends T> getter, BiConsumer<? super O, ? super T> setter) {
            this.getter = getter;
            this.setter = setter;
        }

        public Builder<T, O> withBehavior(EditableValueBehavior<T> behavior) {
            this.behavior = behavior;
            return this;
        }

        public Builder<T, O> withCustomId(String id) {
            this.id = id;
            return this;
        }

        public Builder<T, O> withDefaultValue(T defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public EditableValue<T, O> build(String name) {
            if (behavior == null) {
                throw new IllegalStateException("Behavior must be set before building EditableValue");
            }
            if (defaultValue == null) {
                throw new IllegalStateException("Default value must be set before building EditableValue");
            }
            return new EditableValueImpl<>(name, id, getter, setter, defaultValue, behavior);
        }
    }
}

