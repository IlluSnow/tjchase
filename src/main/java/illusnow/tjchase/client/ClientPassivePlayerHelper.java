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

package illusnow.tjchase.client;

import com.google.common.collect.ImmutableSet;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import org.jspecify.annotations.Nullable;

import java.util.Set;

public final class ClientPassivePlayerHelper {
    @Nullable
    private static Set<KeyMapping> disabledKeys = null;
    @Nullable
    private static Set<Identifier> disabledLayers = null;

    private ClientPassivePlayerHelper() {}

    public static boolean isKeyDisabled(KeyMapping key) {
        if (disabledKeys == null) {
            Options options = Minecraft.getInstance().options;
            ImmutableSet.Builder<KeyMapping> builder = ImmutableSet.builder();
            builder.add(
                    options.keyInventory,
                    options.keyJump,
                    options.keyShift,
                    options.keySprint,
                    options.keySwapOffhand,
                    options.keyDrop,
                    options.keyUse,
                    options.keyAttack,
                    options.keyPickItem
            );
            builder.add(options.keyHotbarSlots);
            disabledKeys = builder.build();
        }
        return disabledKeys.contains(key);
    }

    public static boolean isGuiLayerDisabled(Identifier layerName) {
        if (disabledLayers == null) {
            ImmutableSet.Builder<Identifier> builder = ImmutableSet.builder();
            builder.add(
                    VanillaGuiLayers.AIR_LEVEL,
                    VanillaGuiLayers.ARMOR_LEVEL,
                    VanillaGuiLayers.CROSSHAIR,
                    VanillaGuiLayers.EXPERIENCE_LEVEL,
                    VanillaGuiLayers.CONTEXTUAL_INFO_BAR,
                    VanillaGuiLayers.CONTEXTUAL_INFO_BAR_BACKGROUND,
                    VanillaGuiLayers.FOOD_LEVEL,
                    VanillaGuiLayers.HOTBAR,
                    VanillaGuiLayers.PLAYER_HEALTH,
                    VanillaGuiLayers.SELECTED_ITEM_NAME,
                    VanillaGuiLayers.VEHICLE_HEALTH
            );
            disabledLayers = builder.build();
        }
        return disabledLayers.contains(layerName);
    }
}
