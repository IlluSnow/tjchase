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
import net.minecraft.network.chat.Component;

public final class ModComponents {
    public static final String SPACED_LEFT_PARENTHESIS_MSG = TJChase.prefixMsg("spaced_left_parenthesis");
    public static final String SPACED_RIGHT_PARENTHESIS_MSG = TJChase.prefixMsg("spaced_right_parenthesis");
    public static final Component SPACED_LEFT_PARENTHESIS = Component.translatable(SPACED_LEFT_PARENTHESIS_MSG);
    public static final Component SPACED_RIGHT_PARENTHESIS = Component.translatable(SPACED_RIGHT_PARENTHESIS_MSG);

    private ModComponents() {}
}
