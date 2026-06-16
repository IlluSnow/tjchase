package illusnow.tjchase.client.renderer.entity;

import illusnow.tjchase.TJChase;
import illusnow.tjchase.entity.ModEntities;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = TJChase.MODID)
public final class ModEntityRenderers {
    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.HARP_TESTER.get(), DummyEntityRenderer::new);
        event.registerEntityRenderer(ModEntities.ORBITING_BLOCK.get(), OrbitingBlockRenderer::new);
        event.registerEntityRenderer(ModEntities.VINE_MANAGER.get(), DummyEntityRenderer::new);
        event.registerEntityRenderer(ModEntities.VINE_SEED.get(), ThrownItemRenderer::new);
    }

    private ModEntityRenderers() {}
}
