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

package illusnow.tjchase.data;

import illusnow.tjchase.TJChase;
import illusnow.tjchase.block.ModBlocks;
import illusnow.tjchase.tag.ModBlockTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.data.BlockTagsProvider;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagsProvider extends BlockTagsProvider {
    public ModBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, TJChase.MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(ModBlockTags.TEMPORARY_BLOCKS_OF_VINES).add(
                ModBlocks.TEMPORARY_ACACIA_LEAVES.get(),
                ModBlocks.TEMPORARY_AZALEA_LEAVES.get(),
                ModBlocks.TEMPORARY_BIRCH_LEAVES.get(),
                ModBlocks.TEMPORARY_DARK_OAK_LEAVES.get(),
                ModBlocks.TEMPORARY_FLOWERING_AZALEA_LEAVES.get(),
                ModBlocks.TEMPORARY_JUNGLE_LEAVES.get(),
                ModBlocks.TEMPORARY_MOSS_BLOCK.get(),
                ModBlocks.TEMPORARY_OAK_LEAVES.get(),
                ModBlocks.TEMPORARY_SPRUCE_LEAVES.get(),
                ModBlocks.TEMPORARY_VINE.get()
        );
        tag(BlockTags.CLIMBABLE).add(ModBlocks.TEMPORARY_VINE.get());
        tag(ModBlockTags.SPEED_BOOSTING_VINE).add(ModBlocks.TEMPORARY_VINE.get());
        tag(BlockTags.LEAVES).add(
                ModBlocks.TEMPORARY_ACACIA_LEAVES.get(),
                ModBlocks.TEMPORARY_AZALEA_LEAVES.get(),
                ModBlocks.TEMPORARY_BIRCH_LEAVES.get(),
                ModBlocks.TEMPORARY_DARK_OAK_LEAVES.get(),
                ModBlocks.TEMPORARY_FLOWERING_AZALEA_LEAVES.get(),
                ModBlocks.TEMPORARY_JUNGLE_LEAVES.get(),
                ModBlocks.TEMPORARY_OAK_LEAVES.get(),
                ModBlocks.TEMPORARY_SPRUCE_LEAVES.get()
        );
        tag(BlockTags.MINEABLE_WITH_HOE).add(
                ModBlocks.TEMPORARY_ACACIA_LEAVES.get(),
                ModBlocks.TEMPORARY_AZALEA_LEAVES.get(),
                ModBlocks.TEMPORARY_BIRCH_LEAVES.get(),
                ModBlocks.TEMPORARY_DARK_OAK_LEAVES.get(),
                ModBlocks.TEMPORARY_FLOWERING_AZALEA_LEAVES.get(),
                ModBlocks.TEMPORARY_JUNGLE_LEAVES.get(),
                ModBlocks.TEMPORARY_MOSS_BLOCK.get(),
                ModBlocks.TEMPORARY_OAK_LEAVES.get(),
                ModBlocks.TEMPORARY_SPRUCE_LEAVES.get()
        );
        tag(BlockTags.DIRT).add(ModBlocks.TEMPORARY_MOSS_BLOCK.get());
        tag(BlockTags.SMALL_DRIPLEAF_PLACEABLE).add(ModBlocks.TEMPORARY_MOSS_BLOCK.get());
        tag(BlockTags.SNIFFER_DIGGABLE_BLOCK).add(ModBlocks.TEMPORARY_MOSS_BLOCK.get());
        tag(BlockTags.SNIFFER_EGG_HATCH_BOOST).add(ModBlocks.TEMPORARY_MOSS_BLOCK.get());
        tag(BlockTags.MINEABLE_WITH_AXE).add(ModBlocks.TEMPORARY_VINE.get());
        tag(BlockTags.SWORD_EFFICIENT).add(ModBlocks.TEMPORARY_VINE.get());
        tag(BlockTags.MANGROVE_LOGS_CAN_GROW_THROUGH).add(ModBlocks.TEMPORARY_VINE.get());
        tag(BlockTags.MANGROVE_ROOTS_CAN_GROW_THROUGH).add(ModBlocks.TEMPORARY_VINE.get());
        tag(BlockTags.REPLACEABLE_BY_TREES).add(ModBlocks.TEMPORARY_VINE.get());
        tag(ModBlockTags.SOFT_ORBITING_BLOCK_TAG_SPECIAL).add(Blocks.COBWEB, Blocks.VINE, Blocks.GLOW_LICHEN);
    }
}
