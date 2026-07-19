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

package illusnow.tjchase.tag;

import illusnow.tjchase.TJChase;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public final class ModBlockTags {
    public static final TagKey<Block> TEMPORARY_BLOCKS_OF_VINES = create("temporary_blocks_of_vines");
    public static final TagKey<Block> SPEED_BOOSTING_VINE = create("speed_boosting_vine");
    public static final TagKey<Block> EXPLOSIVE_ORBITING_BLOCK_TAG_SPECIAL = create("explosive_orbiting_block_tag_special");
    public static final TagKey<Block> HEAVY_ORBITING_BLOCK_TAG_SPECIAL = create("heavy_orbiting_block_tag_special");
    public static final TagKey<Block> HARD_ORBITING_BLOCK_TAG_SPECIAL = create("hard_orbiting_block_tag_special");
    public static final TagKey<Block> SOFT_ORBITING_BLOCK_TAG_SPECIAL = create("soft_orbiting_block_tag_special");

    private ModBlockTags() {}

    private static TagKey<Block> create(String name) {
        return BlockTags.create(TJChase.prefix(name));
    }
}
