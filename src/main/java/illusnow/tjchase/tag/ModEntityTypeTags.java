package illusnow.tjchase.tag;

import illusnow.tjchase.TJChase;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public final class ModEntityTypeTags {
    public static final TagKey<EntityType<?>> ZURI_FULLY_CONTROLLABLE = create("zuri_fully_controllable");
    public static final TagKey<EntityType<?>> ZURI_PARTIALLY_CONTROLLABLE = create("zuri_partially_controllable");
    public static final TagKey<EntityType<?>> ZURI_UNCONTROLLABLE = create("zuri_uncontrollable");

    private ModEntityTypeTags() {}

    private static TagKey<EntityType<?>> create(String name) {
        return TagKey.create(Registries.ENTITY_TYPE, TJChase.prefix(name));
    }
}
