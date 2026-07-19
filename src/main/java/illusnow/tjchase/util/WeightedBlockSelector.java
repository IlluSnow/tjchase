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

import com.mojang.serialization.Codec;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class WeightedBlockSelector {
    public static final Codec<WeightedBlockSelector> CODEC = WeightedBlock.CODEC.listOf().xmap(WeightedBlockSelector::new, selector -> selector.blocks);

    private final List<WeightedBlock> blocks;

    public WeightedBlockSelector(WeightedBlock... blocks) {
        this(List.of(blocks));
    }

    public WeightedBlockSelector(List<WeightedBlock> blocks) {
        this.blocks = blocks;
    }

    public BlockState getRandomMaterial(RandomSource random) {
        return WeightedRandom.getRandomItem(random, blocks, WeightedBlock::weight).orElseThrow().blockState();
    }
}
