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

package illusnow.tjchase.tag;

import illusnow.tjchase.TJChase;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public final class ModEntityTypeTags {
    public static final TagKey<EntityType<?>> ZURI_FULLY_CONTROLLABLE = create("zuri_fully_controllable");
    public static final TagKey<EntityType<?>> ZURI_PARTIALLY_CONTROLLABLE = create("zuri_partially_controllable");
    public static final TagKey<EntityType<?>> ZURI_UNCONTROLLABLE = create("zuri_uncontrollable");

    private ModEntityTypeTags() {}

    private static TagKey<EntityType<?>> create(String name) {
        return TagKey.create(Registries.ENTITY_TYPE, TJChase.prefix(name));
    }
}
