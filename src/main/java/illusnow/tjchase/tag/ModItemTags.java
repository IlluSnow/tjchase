package illusnow.tjchase.tag;

import illusnow.tjchase.TJChase;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class ModItemTags {
    public static final TagKey<Item> HARPS = create("harps");

    private ModItemTags() {}

    private static TagKey<Item> create(String name) {
        return ItemTags.create(TJChase.prefix(name));
    }
}
