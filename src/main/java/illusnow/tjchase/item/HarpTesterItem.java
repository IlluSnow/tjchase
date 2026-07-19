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

import illusnow.tjchase.entity.HarpTester;
import illusnow.tjchase.entity.ModEntities;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

public class HarpTesterItem extends Item {
    public HarpTesterItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        List<HarpTester> harpTesters = HarpTester.getHarpTestersNearby(level, player);
        if (!harpTesters.isEmpty()) {
            return super.use(level, player, hand);
        }
        if (!level.isClientSide()) {
            HarpTester harpTester = ModEntities.HARP_TESTER.get().create(level, EntitySpawnReason.EVENT);
            if (harpTester == null) {
                return super.use(level, player, hand);
            }
            harpTester.setPos(player.position());
            harpTester.setTarget(player);
            level.addFreshEntity(harpTester);
            player.awardStat(Stats.ITEM_USED.get(this));
        }
        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
