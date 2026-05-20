package illusnow.tjchase.tag;

import illusnow.tjchase.TJChase;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public final class ModBlockTags {
    public static final TagKey<Block> TEMPORARY_BLOCKS_OF_VINES = create("temporary_blocks_of_vines");
    public static final TagKey<Block> SPEED_BOOSTING_VINE = create("speed_boosting_vine");

    private ModBlockTags() {}

    private static TagKey<Block> create(String name) {
        return BlockTags.create(TJChase.prefix(name));
    }
}
