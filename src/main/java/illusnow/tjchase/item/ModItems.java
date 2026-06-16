package illusnow.tjchase.item;

import illusnow.tjchase.TJChase;
import illusnow.tjchase.item.enchantment.EntityDebugStickItem;
import illusnow.tjchase.util.HarpConstants;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;
import java.util.function.UnaryOperator;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.createItems(TJChase.MODID);
    public static final DeferredHolder<Item, EntityDebugStickItem> ENTITY_DEBUG_STICK = register(ModItemNames.ENTITY_DEBUG_STICK, EntityDebugStickItem::new,
            properties -> properties.stacksTo(1)
                    .rarity(Rarity.EPIC)
                    .component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true));
    public static final DeferredHolder<Item, HarpItem> HARP = ITEMS.register(ModItemNames.HARP, () ->
            new HarpItem(new Item.Properties().setId(createItemId(ModItemNames.HARP)).durability(HarpConstants.DURABILITY).enchantable(HarpConstants.ENCHANTMENT_VALUE).repairable(ItemTags.GOLD_TOOL_MATERIALS),
                    HarpConstants.BASE_ORBITING_BLOCK_COUNT,
                    0));
    public static final DeferredHolder<Item, HarpItem> NETHERITE_HARP = ITEMS.register(ModItemNames.NETHERITE_HARP, () ->
            new HarpItem(new Item.Properties().setId(createItemId(ModItemNames.NETHERITE_HARP)).fireResistant().durability(HarpConstants.DURABILITY_NETHERITE).enchantable(HarpConstants.ENCHANTMENT_VALUE_NETHERITE).repairable(ItemTags.NETHERITE_TOOL_MATERIALS),
                    HarpConstants.BASE_ORBITING_BLOCK_COUNT + HarpConstants.ORBITING_BLOCK_COUNT_NETHERITE_BONUS,
                    HarpConstants.ORBITING_BLOCK_DAMAGE_NETHERITE_BONUS));
    public static final DeferredHolder<Item, HarpTesterItem> HARP_TESTER = register(ModItemNames.HARP_TESTER, HarpTesterItem::new,
            properties -> properties.stacksTo(1)
                    .rarity(Rarity.EPIC)
                    .component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true));
    public static final DeferredHolder<Item, Item> VINE_SEED = register(ModItemNames.VINE_SEED, VineSeedItem::new, UnaryOperator.identity());

    private ModItems() {}

    private static DeferredHolder<Item, Item> register(String name, UnaryOperator<Item.Properties> op) {
        return register(name, Item::new, op);
    }

    private static <T extends Item> DeferredHolder<Item, T> register(String name, Function<? super Item.Properties, ? extends T> factory, UnaryOperator<Item.Properties> op) {
        return ITEMS.register(name, () -> factory.apply(op.apply(new Item.Properties().setId(createItemId(name)))));
    }

    private static ResourceKey<Item> createItemId(String name) {
        return ResourceKey.create(Registries.ITEM, TJChase.prefix(name));
    }
}
