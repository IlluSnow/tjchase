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

package illusnow.tjchase.world.gameplay.action;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

public non-sealed class ContinuousAction extends Action {
    public ContinuousAction(Identifier id, Identifier animId, int priority) {
        super(id, animId, priority);
    }

    @Override
    public void onComplete(Player player, ActionHolder holder) {}

    @Override
    public void onInterrupt(Player player, ActionHolder holder) {}

    @Override
    public void update(Player player, ActionHolder holder) {}
}
