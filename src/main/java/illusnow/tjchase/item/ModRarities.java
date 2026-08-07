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

import illusnow.tjchase.TJChase;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Rarity;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;

import java.util.function.UnaryOperator;

public final class ModRarities {
    public static final EnumProxy<Rarity> BLUEPRINT_CUSTOM = new EnumProxy<>(
            Rarity.class, -1, prefixCustom(ModItemNames.BLUEPRINT), (UnaryOperator<Style>) style ->
            style.withItalic(true).withColor(ChatFormatting.BLUE)
    );
    public static final EnumProxy<Rarity> HARP_CUSTOM = new EnumProxy<>(
            Rarity.class, -1, prefixCustom(ModItemNames.HARP), (UnaryOperator<Style>) style ->
            style.withItalic(true).withColor(ChatFormatting.GOLD)
    );
    public static final EnumProxy<Rarity> NETHERITE_HARP_CUSTOM = new EnumProxy<>(
            Rarity.class, -1, prefixCustom(ModItemNames.NETHERITE_HARP), (UnaryOperator<Style>) style ->
            style.withItalic(true).withColor(ChatFormatting.LIGHT_PURPLE)
    );
    public static final EnumProxy<Rarity> VINE_SEED_CUSTOM = new EnumProxy<>(
            Rarity.class, -1, prefixCustom(ModItemNames.VINE_SEED), (UnaryOperator<Style>) style ->
            style.withItalic(true).withColor(ChatFormatting.GREEN)
    );

    private ModRarities() {}

    private static String prefixCustom(String name) {
        return TJChase.prefixEnum(name + "_custom");
    }
}
