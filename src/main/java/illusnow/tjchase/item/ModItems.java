package illusnow.tjchase.item;

import illusnow.tjchase.TJChase;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;
import java.util.function.UnaryOperator;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.createItems(TJChase.MODID);
    public static final DeferredHolder<Item, Item> VINE_SEED
            = register(ModItemNames.VINE_SEED, VineSeedItem::new, UnaryOperator.identity());

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
