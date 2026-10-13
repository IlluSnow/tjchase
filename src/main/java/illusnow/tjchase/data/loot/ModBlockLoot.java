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

package illusnow.tjchase.data.loot;

import illusnow.tjchase.block.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.HashSet;
import java.util.Set;

public class ModBlockLoot extends BlockLootSubProvider {
    private static final Set<Item> EXPLOSION_RESISTANT = Set.of();
    private final Set<Block> knownBlocks = new HashSet<>();

    public ModBlockLoot(HolderLookup.Provider registries) {
        super(EXPLOSION_RESISTANT, FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        dropSelf(ModBlocks.MAILBOX.get());
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return knownBlocks;
    }

    protected void add(Block block, LootTable.Builder builder) {
        super.add(block, builder);
        knownBlocks.add(block);
    }
}
