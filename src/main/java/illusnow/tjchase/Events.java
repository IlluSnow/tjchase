package illusnow.tjchase;

import illusnow.tjchase.tag.ModBlockTags;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;

@EventBusSubscriber(modid = TJChase.MODID)
public class Events {
    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        if (event.getEntity().level().getBlockState(event.getEntity().getOnPos()).is(ModBlockTags.TEMPORARY_BLOCKS_OF_VINES)) {
            event.setDamageMultiplier(0);
        }
    }
}
