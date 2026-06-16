package illusnow.tjchase.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.block.state.BlockState;

public record WeightedBlock(BlockState blockState, int weight) {
    public static final Codec<WeightedBlock> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    BlockState.CODEC.fieldOf("block_state").forGetter(WeightedBlock::blockState),
                    Codec.INT.fieldOf("weight").forGetter(WeightedBlock::weight)
            ).apply(instance, WeightedBlock::new)
    );

    @Override
    public int weight() {
        return Math.max(0, weight);
    }
}
