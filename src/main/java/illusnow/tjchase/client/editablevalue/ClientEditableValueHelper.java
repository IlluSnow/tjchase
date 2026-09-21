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

package illusnow.tjchase.client.editablevalue;

import illusnow.tjchase.client.gui.EditableValueList;
import illusnow.tjchase.world.gameplay.object.editablevalue.EditableValue;
import illusnow.tjchase.world.gameplay.object.editablevalue.ModEditableValues;
import net.minecraft.util.StringRepresentable;
import net.neoforged.neoforge.common.TranslatableEnum;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class ClientEditableValueHelper {
    private static final Map<EditableValue<?, ?>, EntryCreator<?, ?>> FACTORIES = new IdentityHashMap<>();

    private ClientEditableValueHelper() {}

    public static void registerFactories() {
        registerFactory(ModEditableValues.BURNING_SPEED, ClientEditableValueHelper::createDoubleEditBox);
        registerFactory(ModEditableValues.DEFAULT_FUSE_SECONDS, ClientEditableValueHelper::createIntEditBox);
        registerFactory(ModEditableValues.INSTABURN_SECONDS, ClientEditableValueHelper::createIntEditBox);
        registerFactory(ModEditableValues.FUSE_DISPLAY_DIRECTION, ClientEditableValueHelper::createEnumCycleButton);
        registerFactory(ModEditableValues.FUSE_DISPLAY_DISTANCE, ClientEditableValueHelper::createDoubleEditBox);
        registerFactory(ModEditableValues.FUSE_DISPLAY_HEIGHT_OFFSET, ClientEditableValueHelper::createDoubleEditBox);
        registerFactory(ModEditableValues.FUSE_DISPLAY_FONT_SCALE, ClientEditableValueHelper::createDoubleEditBox);
    }

    public static <T, O> void registerFactory(Supplier<? extends EditableValue<T, O>> sup, EntryCreator<T, O> factory) {
        registerFactory(sup.get(), factory);
    }

    public static <T, O> void registerFactory(EditableValue<T, O> key, EntryCreator<T, O> factory) {
        FACTORIES.put(key, factory);
    }

    @SuppressWarnings("unchecked")
    public static <T, O> EditableValueList.EditableValueEntry createEntry(EditableValueList<O> list, EditableValue<T, O> key, Map<EditableValue<?, O>, Object> tempValueMap) {
        EntryCreator<T, O> entryCreator = (EntryCreator<T, O>) FACTORIES.get(key);
        return entryCreator.createEntry(list, key, tempValueMap);
    }

    public static <O> EditableValueList.EditableValueEntry createIntEditBox(EditableValueList<O> l, EditableValue<Integer, O> v, Map<EditableValue<?, O>, Object> t) {
        return new EditableValueList.NumberEditBoxEntry(l, v.createTranslation(), l.getTemplate(), v, false, s -> t.put(v, Integer.valueOf(s)));
    }

    public static <O> EditableValueList.EditableValueEntry createDoubleEditBox(EditableValueList<O> l, EditableValue<Double, O> v, Map<EditableValue<?, O>, Object> t) {
        return new EditableValueList.NumberEditBoxEntry(l, v.createTranslation(), l.getTemplate(), v, true, s -> t.put(v, Double.valueOf(s)));
    }

    public static <E extends Enum<E> & StringRepresentable & TranslatableEnum, O> EditableValueList.EditableValueEntry createEnumCycleButton(EditableValueList<O> l, EditableValue<E, O> v, Map<EditableValue<?, O>, Object> t) {
        return new EditableValueList.EnumEntry<>(l, v.createTranslation(), l.getTemplate(), v, e -> t.put(v, e));
    }

    @FunctionalInterface
    public interface EntryCreator<T, O> {
        EditableValueList.EditableValueEntry createEntry(EditableValueList<O> list, EditableValue<T, O> editableValue, Map<EditableValue<?, O>, Object> tempValueMap);
    }
}
