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

import illusnow.tjchase.TJChase;
import illusnow.tjchase.world.gameplay.action.Action;
import illusnow.tjchase.world.gameplay.object.GameplayObjectType;
import illusnow.tjchase.world.gameplay.object.editablevalue.EditableValue;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.RegistryBuilder;

public final class ModRegistries {
    public static final ResourceKey<Registry<Action>> ACTIONS_KEY = ResourceKey.createRegistryKey(TJChase.prefix("actions"));
    public static final Registry<Action> ACTIONS = new RegistryBuilder<>(ModRegistries.ACTIONS_KEY).sync(true).create();
    public static final ResourceKey<Registry<EditableValue<?, ?>>> EDITABLE_VALUES_KEY = ResourceKey.createRegistryKey(TJChase.prefix("editable_values"));
    public static final Registry<EditableValue<?, ?>> EDITABLE_VALUES = new RegistryBuilder<>(ModRegistries.EDITABLE_VALUES_KEY).sync(true).create();
    public static final ResourceKey<Registry<GameplayObjectType<?>>> GAMEPLAY_OBJECT_TYPES_KEY = ResourceKey.createRegistryKey(TJChase.prefix("gameplay_object_types"));
    public static final Registry<GameplayObjectType<?>> GAMEPLAY_OBJECT_TYPES = new RegistryBuilder<>(ModRegistries.GAMEPLAY_OBJECT_TYPES_KEY).sync(true).create();

    private ModRegistries() {}
}
