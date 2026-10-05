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

package illusnow.tjchase.world.gameplay.struggle;

import illusnow.tjchase.entity.gameplay.TyingHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class PlayerStruggleType extends StruggleType {
    public PlayerStruggleType(ItemStack icon, int maxStruggle) {
        super(icon, maxStruggle);
    }

    @Override
    public boolean shouldRemove(Player player) {
        return !(TyingHelper.getTiedTo(player) instanceof Player);
    }

    @Override
    public void onSuccessfullyStruggled(Player player) {
        Entity tiedTo = TyingHelper.getTiedTo(player);
        if (tiedTo instanceof Player playerTiedTo) {
            TyingHelper.tie(player, null);
            TyingHelper.clearHugOrTieAction(playerTiedTo);
            TyingHelper.clearStruggleOrPrayAction(player);
        }
    }
}
