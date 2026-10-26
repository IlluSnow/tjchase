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

package illusnow.tjchase.world.vip;

import com.google.common.collect.ImmutableList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.List;

public record VIPReward(Item primary, int initialRewardCount, Item... others) {
    public List<ItemStack> createDailyReward(boolean includePrimary) {
        List<ItemStack> nonPrimaryStacks = Arrays.stream(others).map(ItemStack::new).toList();
        if (!includePrimary) {
            return nonPrimaryStacks;
        }
        ItemStack primaryStack = new ItemStack(primary);
        return ImmutableList.<ItemStack>builder().add(primaryStack).addAll(nonPrimaryStacks).build();
    }

    public ItemStack getInitialReward() {
        return new ItemStack(primary, initialRewardCount);
    }
}
