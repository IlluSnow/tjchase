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

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Rarity;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;

import java.util.function.UnaryOperator;

import static illusnow.tjchase.TJChase.prefixEnum;

public final class ModRarities {
    public static final EnumProxy<Rarity> GRAY = new EnumProxy<>(
            Rarity.class, -1, prefixEnum("gray"), (UnaryOperator<Style>) style ->
            style.withItalic(true).withColor(ChatFormatting.GRAY)
    );
    public static final EnumProxy<Rarity> BLUE = new EnumProxy<>(
            Rarity.class, -1, prefixEnum("blue"), (UnaryOperator<Style>) style ->
            style.withItalic(true).withColor(ChatFormatting.BLUE)
    );
    public static final EnumProxy<Rarity> GOLD = new EnumProxy<>(
            Rarity.class, -1, prefixEnum("gold"), (UnaryOperator<Style>) style ->
            style.withItalic(true).withColor(ChatFormatting.GOLD)
    );
    public static final EnumProxy<Rarity> LIGHT_PURPLE = new EnumProxy<>(
            Rarity.class, -1, prefixEnum("light_purple"), (UnaryOperator<Style>) style ->
            style.withItalic(true).withColor(ChatFormatting.LIGHT_PURPLE)
    );
    public static final EnumProxy<Rarity> GREEN = new EnumProxy<>(
            Rarity.class, -1, prefixEnum("green"), (UnaryOperator<Style>) style ->
            style.withItalic(true).withColor(ChatFormatting.GREEN)
    );
    public static final EnumProxy<Rarity> RED = new EnumProxy<>(
            Rarity.class, -1, prefixEnum("red"), (UnaryOperator<Style>) style ->
            style.withItalic(true).withColor(ChatFormatting.RED)
    );
    public static final EnumProxy<Rarity> JERRY_CUSTOM = new EnumProxy<>(
            Rarity.class, -1, prefixCustom("jerry"), (UnaryOperator<Style>) style ->
            style.withColor(0xE89E6B)
    );
    public static final EnumProxy<Rarity> TOM_CUSTOM = new EnumProxy<>(
            Rarity.class, -1, prefixCustom("tom"), (UnaryOperator<Style>) style ->
            style.withColor(0x8EACDE)
    );

    private ModRarities() {}

    private static String prefixCustom(String name) {
        return prefixEnum(name + "_custom");
    }
}
