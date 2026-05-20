package illusnow.tjchase.item;

import illusnow.tjchase.TJChase;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeModeTabs {
    public static final String RANDOM_CREATIONS_TAB_ID = "itemGroups." + TJChase.MODID;
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TJChase.MODID);
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = CREATIVE_MODE_TABS.register(TJChase.MODID + "_tab", () -> CreativeModeTab.builder()
            .icon(() -> ModItems.VINE_SEED.get().getDefaultInstance())
            .title(Component.translatable(RANDOM_CREATIONS_TAB_ID))
            .build());

    private ModCreativeModeTabs() {}
}
