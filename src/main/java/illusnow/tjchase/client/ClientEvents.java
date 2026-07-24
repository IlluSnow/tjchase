/*
 * Copyright 2026 IlluSnow
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package illusnow.tjchase.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import illusnow.tjchase.TJChase;
import illusnow.tjchase.client.resources.sounds.DanceTimeSoundInstance;
import illusnow.tjchase.client.util.OrbitingBlocksRenderHelper;
import illusnow.tjchase.entity.Zuri;
import illusnow.tjchase.util.DancingHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.joml.Quaternionf;

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

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof Zuri zuri && zuri.level().isClientSide() && zuri.isDancing() && zuri.tickCount == 1) {
            zuriClientFirstTick(zuri);
        }
    }

    private static void zuriClientFirstTick(Zuri zuri) {
        if (!zuri.isSilent()) {
            Minecraft.getInstance().getSoundManager().play(new DanceTimeSoundInstance(zuri));
        }
    }

    @SubscribeEvent
    public static void onRenderLiving(RenderLivingEvent.Post<?, ?, ?> event) {
        LivingEntityRenderState renderState = event.getRenderState();
        DancingHelper.DanceEffectType danceEffectType = ((DanceEffectTypeOperator) renderState).tjChase$getDanceEffectType();
        if (renderState.isInvisible || danceEffectType == DancingHelper.DanceEffectType.NONE) {
            return;
        }
        Identifier textureLocation = danceEffectType.getTextureLocation();
        Objects.requireNonNull(textureLocation);
        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        float yOffset = renderState.boundingBoxHeight * 1.2F + 0.2F;
        poseStack.translate(0, yOffset, 0);
        Quaternionf rotation = Minecraft.getInstance().gameRenderer.getMainCamera().rotation();
        poseStack.mulPose(new Quaternionf(0, rotation.y, 0, rotation.w));

        float size = 0.5F;
        event.getSubmitNodeCollector().submitCustomGeometry(poseStack,
                RenderTypes.entityCutoutNoCull(textureLocation, false),
                (pose, consumer) -> {
                    dancingEffectIconVertex(pose, consumer, renderState, -size / 2, -size / 2, 0, 1);
                    dancingEffectIconVertex(pose, consumer, renderState, size / 2, -size / 2, 1, 1);
                    dancingEffectIconVertex(pose, consumer, renderState, size / 2, size / 2, 1, 0);
                    dancingEffectIconVertex(pose, consumer, renderState, -size / 2, size / 2, 0, 0);
                }
        );
        poseStack.popPose();
    }

    private static void dancingEffectIconVertex(PoseStack.Pose pose, VertexConsumer consumer, LivingEntityRenderState renderState, float x, float y, float u, float v) {
        consumer.addVertex(pose, x, y, 0)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(renderState.lightCoords)
                .setNormal(pose, 0, 1, 0);
    }
}
