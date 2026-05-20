package illusnow.tjchase;

import illusnow.tjchase.item.ModCreativeModeTabs;
import illusnow.tjchase.item.ModItems;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

@EventBusSubscriber(modid = TJChase.MODID)
public class ModEvents {
    @SubscribeEvent
    public static void onBuildCreativeModeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTab() == ModCreativeModeTabs.TAB.get()) {
            event.accept(ModItems.VINE_SEED.get());
        }
    }
}
