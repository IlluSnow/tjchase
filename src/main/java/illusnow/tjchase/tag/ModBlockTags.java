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
