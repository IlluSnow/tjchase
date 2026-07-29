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

import illusnow.tjchase.TJChase;
import illusnow.tjchase.client.resources.sounds.DanceTimeSoundInstance;
import illusnow.tjchase.client.util.OrbitingBlocksRenderHelper;
import illusnow.tjchase.entity.BlueprintManager;
import illusnow.tjchase.entity.Zuri;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

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
    public static void onComputeFogColor(ViewportEvent.ComputeFogColor event) {
        if (isPlayerInBlueprint()) {
            float r = 0.22f;
            float g = 0.40f;
            float b = 0.72f;

            event.setRed(Mth.lerp(0.7F, event.getRed(), r));
            event.setGreen(Mth.lerp(0.7F, event.getGreen(), g));
            event.setBlue(Mth.lerp(0.7F, event.getBlue(), b));
        }
    }

    @SubscribeEvent
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        if (isPlayerInBlueprint()) {
            event.setNearPlaneDistance(Math.min(event.getNearPlaneDistance(), -10));
            event.setFarPlaneDistance(Math.min(event.getFarPlaneDistance(), 20));
        }
    }

    private static boolean isPlayerInBlueprint() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            for (BlueprintManager manager : player.level().getEntitiesOfClass(BlueprintManager.class, player.getBoundingBox().inflate(BlueprintManager.MAX_LENGTH_ALLOWED))) {
                if (manager.isPositionInside(getCameraPosition()) && manager.isValid()) {
                    return true;
                }
            }
        }
        return false;
    }

    private static Vec3 getCameraPosition() {
        return Minecraft.getInstance().gameRenderer.getMainCamera().position();
    }
}
