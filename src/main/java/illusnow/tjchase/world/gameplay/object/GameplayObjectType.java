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

package illusnow.tjchase.world.gameplay.object;

import com.google.common.base.Suppliers;
import illusnow.tjchase.world.gameplay.object.editablevalue.EditableValue;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public final class GameplayObjectType<O extends GameplayObject<?>> {
    private final Supplier<? extends Component> defaultName;
    private final List<EditableValue<?, O>> editableValues;

    private GameplayObjectType(Supplier<? extends Component> defaultName, List<EditableValue<?, O>> editableValues) {
        this.defaultName = defaultName;
        this.editableValues = editableValues;
    }

    public Component getDefaultName() {
        return defaultName.get();
    }

    public List<EditableValue<?, O>> getEditableValues() {
        return editableValues;
    }

    public void serialize(ValueOutput output, O obj) {
        for (EditableValue<?, O> editableValue : getEditableValues()) {
            editableValue.serialize(output, obj);
        }
    }

    public void deserialize(ValueInput input, O obj) {
        for (EditableValue<?, O> editableValue : getEditableValues()) {
            editableValue.deserialize(input, obj);
        }
    }

    public String toString() {
        return "GameplayObjectType{defaultName=" + defaultName + ", editableValues=" + editableValues + "}";
    }

    public static <O extends Entity & GameplayObject<?>> Builder<O> fromEntity(Supplier<EntityType<O>> typeSupplier) {
        return withDefaultName(() -> typeSupplier.get().getDescription());
    }

    public static <O extends GameplayObject<?>> Builder<O> withDefaultName(Supplier<Component> defaultName) {
        return new Builder<>(defaultName);
    }

    public static final class Builder<O extends GameplayObject<?>> {
        private final Supplier<Component> defaultName;
        private final List<EditableValue<?, O>> editableValues = new ArrayList<>();

        @SuppressWarnings("NullableProblems")
        private Builder(Supplier<Component> defaultName) {
            this.defaultName = Suppliers.memoize(() -> Objects.requireNonNull(defaultName.get(), "defaultName cannot be null"));
        }

        public <T> Builder<O> add(EditableValue<T, O> editableValue) {
            editableValues.add(editableValue);
            return this;
        }

        public GameplayObjectType<O> build() {
            return new GameplayObjectType<>(defaultName, editableValues);
        }
    }
}
