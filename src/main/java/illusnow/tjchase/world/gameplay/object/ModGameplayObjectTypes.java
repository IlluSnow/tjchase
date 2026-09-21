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

import illusnow.tjchase.TJChase;
import illusnow.tjchase.entity.ModEntities;
import illusnow.tjchase.entity.gameplay.Rocket;
import illusnow.tjchase.util.ModRegistries;
import illusnow.tjchase.world.gameplay.object.editablevalue.ModEditableValues;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ModGameplayObjectTypes {
    public static final DeferredRegister<GameplayObjectType<?>> GAMEPLAY_OBJECT_TYPES = DeferredRegister.create(ModRegistries.GAMEPLAY_OBJECT_TYPES, TJChase.MODID);
    public static final DeferredHolder<GameplayObjectType<?>, GameplayObjectType<Rocket>> ROCKET = register("rocket",
            () -> GameplayObjectType.fromEntity(ModEntities.ROCKET)
                    .add(ModEditableValues.DEFAULT_FUSE_SECONDS.get())
                    .add(ModEditableValues.INSTABURN_SECONDS.get())
                    .add(ModEditableValues.BURNING_SPEED.get())
                    .add(ModEditableValues.FUSE_DISPLAY_DIRECTION.get())
                    .add(ModEditableValues.FUSE_DISPLAY_DISTANCE.get())
                    .add(ModEditableValues.FUSE_DISPLAY_HEIGHT_OFFSET.get())
                    .add(ModEditableValues.FUSE_DISPLAY_FONT_SCALE.get()));

    private ModGameplayObjectTypes() {}

    private static <T extends GameplayObject<?>> DeferredHolder<GameplayObjectType<?>, GameplayObjectType<T>> register(String name, Supplier<GameplayObjectType.Builder<T>> builder) {
        return GAMEPLAY_OBJECT_TYPES.register(name, () -> builder.get().build());
    }
}
