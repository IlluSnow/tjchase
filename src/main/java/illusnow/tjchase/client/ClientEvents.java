package illusnow.tjchase.client;

import illusnow.tjchase.TJChase;
import net.minecraft.world.BossEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;

@EventBusSubscriber(modid = TJChase.MODID, value = Dist.CLIENT)
public class ClientEvents {
    @SubscribeEvent
    public static void onClientSetup(CustomizeGuiOverlayEvent.BossEventProgress event) {
        if (event.getBossEvent().getColor() == BossEvent.BossBarColor.RED
                && event.getBossEvent().getOverlay() == BossEvent.BossBarOverlay.NOTCHED_10) {
            event.setIncrement(5);
        }
    }
}
