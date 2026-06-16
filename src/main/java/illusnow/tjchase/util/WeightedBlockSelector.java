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
