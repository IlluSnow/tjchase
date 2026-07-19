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

import illusnow.tjchase.TJChase;
import net.minecraft.util.context.ContextKey;

public final class ModRenderStateContextKeys {
    public static final ContextKey<Float> MAX_PLAY_HARP_DURATION = new ContextKey<>(TJChase.prefix("max_play_harp_duration"));
    public static final ContextKey<Float> PARTIAL_TICKS = new ContextKey<>(TJChase.prefix("partial_ticks"));

    private ModRenderStateContextKeys() {}
}
