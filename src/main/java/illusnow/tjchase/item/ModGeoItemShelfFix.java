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

package illusnow.tjchase.item;

import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.Map;

public final class ModGeoItemShelfFix {
    private static final Map<Item, Float> Y_TRANSLATION_MAP = new HashMap<>();

    static {
        Y_TRANSLATION_MAP.put(ModItems.MOUSE_EARS.get(), -0.5F);
        Y_TRANSLATION_MAP.put(ModItems.CAT_EARS.get(), -0.5F);
        Y_TRANSLATION_MAP.put(ModItems.REMOTE_CONTROL.get(), -1.075F);
        Y_TRANSLATION_MAP.put(ModItems.MAILBOX.get(), calcYForBlock(14 / 16F));
    }

    public static float getY(Item item) {
        return Y_TRANSLATION_MAP.getOrDefault(item, 0F);
    }

    private static float calcYForBlock(float height) {
        return Mth.lerp(height, -0.5F, -1);
    }
}
