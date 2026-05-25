package illusnow.tjchase.client;

import illusnow.tjchase.TJChase;
import illusnow.tjchase.client.util.OrbitingBlocksRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.Objects;

@EventBusSubscriber(modid = TJChase.MODID, value = Dist.CLIENT)
public class ClientEvents {
    @SubscribeEvent
    public static void onExtractLevelRenderState(ExtractLevelRenderStateEvent event) {
        event.getRenderState().setRenderData(ModRenderStateContextKeys.PARTIAL_TICKS, event.getDeltaTracker().getGameTimeDeltaPartialTick(false));
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent.AfterEntities event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (mc.level == null || player == null || !player.isAlive() || player.isSpectator()) {
            return;
        }
        float partialTicks = Objects.requireNonNull(event.getLevelRenderState().getRenderData(ModRenderStateContextKeys.PARTIAL_TICKS));
        OrbitingBlocksRenderHelper.renderOrbitingBlocks(event.getPoseStack(), event.getLevelRenderState(), mc.getBlockRenderer(), mc.renderBuffers(), player, partialTicks);
    }
}
