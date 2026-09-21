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

import java.util.function.BiConsumer;
import java.util.function.Function;

class EditableValueImpl<T, O> implements EditableValue<T, O> {
    private final String name;
    private final String translationKey;
    private final Function<? super O, ? extends T> getter;
    private final BiConsumer<? super O, ? super T> setter;
    private final T defaultValue;
    private final EditableValueBehavior<T> behavior;

    EditableValueImpl(String name, String id, Function<? super O, ? extends T> getter, BiConsumer<? super O, ? super T> setter, T defaultValue, EditableValueBehavior<T> behavior) {
        this.name = name;
        this.getter = getter;
        this.setter = setter;
        this.defaultValue = defaultValue;
        this.behavior = behavior;
        this.translationKey = "in_game_editable_property." + id + "." + name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public T get(O obj) {
        return getter.apply(obj);
    }

    @Override
    public void set(O obj, T value) {
        setter.accept(obj, getBehavior().sanitize(value));
    }

    @Override
    public T defaultValue() {
        return defaultValue;
    }

    @Override
    public EditableValueBehavior<T> getBehavior() {
        return behavior;
    }

    @Override
    public String getTranslationKey() {
        return translationKey;
    }

    @Override
    public String toString() {
        return translationKey;
    }
}
