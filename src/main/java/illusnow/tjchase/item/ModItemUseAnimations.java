package illusnow.tjchase.item;

import illusnow.tjchase.TJChase;
import net.minecraft.world.item.ItemUseAnimation;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;

public final class ModItemUseAnimations {
    public static final EnumProxy<ItemUseAnimation> HARP_PLAY = create("harp_play", false);

    private ModItemUseAnimations() {}

    private static EnumProxy<ItemUseAnimation> create(String name, boolean customArmTransform) {
        return new EnumProxy<>(ItemUseAnimation.class, 0, TJChase.MODID + ":" + name, customArmTransform);
    }
}
