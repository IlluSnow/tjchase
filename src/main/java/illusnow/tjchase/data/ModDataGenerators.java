package illusnow.tjchase.data;

import illusnow.tjchase.TJChase;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = TJChase.MODID)
public final class ModDataGenerators {
    @SubscribeEvent
    public static void onGatherDataClient(GatherDataEvent.Client event) {
        event.createProvider(ModLanguageProvider.EnUs::new);
        event.createProvider(ModLanguageProvider.ZhCn::new);
        event.createProvider(ModModelProvider::new);
        event.createProvider(ModSoundProvider::new);
        event.createProvider(ModBlockTagsProvider::new);
    }

    private ModDataGenerators() {}
}
