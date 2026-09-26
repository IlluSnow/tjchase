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

package illusnow.tjchase.world.gameplay;

import illusnow.tjchase.TJChase;
import net.minecraft.resources.Identifier;

public final class ModPlayerAnimationIDs {
    public static final Identifier WEAK_LAYER = TJChase.prefix("weak");
    public static final Identifier ACTION_LAYER = TJChase.prefix("actions");

    public static final Identifier TIED_STRUGGLE = TJChase.prefix("tied_struggle");
    public static final Identifier PRAY = TJChase.prefix("pray");
    public static final Identifier HUG = TJChase.prefix("hug");
    public static final Identifier TIE = TJChase.prefix("tie");
    public static final Identifier CRAWL_STILL = TJChase.prefix("crawl_still");
    public static final Identifier CRAWL_MOVING = TJChase.prefix("crawl_moving");

    private ModPlayerAnimationIDs() {}
}
