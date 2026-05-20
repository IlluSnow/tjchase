package illusnow.tjchase.client;

import illusnow.tjchase.TJChase;
import illusnow.tjchase.block.ModBlocks;
import net.minecraft.client.renderer.BiomeColors;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

@EventBusSubscriber(modid = TJChase.MODID, value = Dist.CLIENT)
public class ClientModEvents {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {}

    @SubscribeEvent
    public static void onRegisterBlockColorHandlers(RegisterColorHandlersEvent.Block event) {
        event.register((state, level, pos, tintIndex) -> -8345771, ModBlocks.TEMPORARY_BIRCH_LEAVES.get());
        event.register((state, level, pos, tintIndex) -> -10380959, ModBlocks.TEMPORARY_SPRUCE_LEAVES.get());
        event.register(
                (state, level, pos, tintIndex) -> level != null && pos != null
                        ? BiomeColors.getAverageFoliageColor(level, pos)
                        : -12012264,
                ModBlocks.TEMPORARY_ACACIA_LEAVES.get(),
                ModBlocks.TEMPORARY_DARK_OAK_LEAVES.get(),
                ModBlocks.TEMPORARY_JUNGLE_LEAVES.get(),
                ModBlocks.TEMPORARY_OAK_LEAVES.get(),
                ModBlocks.TEMPORARY_VINE.get()
        );
    }
}
