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
import illusnow.tjchase.client.key.ModKeyMappings;
import illusnow.tjchase.client.network.ModClientPayloadHandlers;
import illusnow.tjchase.client.resources.sounds.DanceTimeSoundInstance;
import illusnow.tjchase.client.resources.sounds.PrimedRocketSoundInstance;
import illusnow.tjchase.client.util.OrbitingBlocksRenderHelper;
import illusnow.tjchase.entity.Zuri;
import illusnow.tjchase.entity.controllable.Controllable;
import illusnow.tjchase.entity.controllable.ControllableMovementHandler;
import illusnow.tjchase.entity.dataentity.BlueprintManager;
import illusnow.tjchase.entity.gameplay.InGamePlacedEntity;
import illusnow.tjchase.entity.gameplay.Rocket;
import illusnow.tjchase.entity.gameplay.TyingHelper;
import illusnow.tjchase.mixin.client.ClientInputAccessor;
import illusnow.tjchase.network.c2s.LoadTemplatePayload;
import illusnow.tjchase.network.c2s.StrugglePayload;
import illusnow.tjchase.network.c2s.UpdateControlledEntityPositionPayload;
import illusnow.tjchase.network.c2s.UpdateInputPayload;
import illusnow.tjchase.util.OriginalInputAccessor;
import illusnow.tjchase.util.Utils;
import illusnow.tjchase.world.gameplay.struggle.StruggleInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.TriState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
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
    public static void canRenderNameTag(RenderNameTagEvent.CanRender event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        Entity tiedTo = TyingHelper.getTiedTo(player);
        if (event.getEntity() == player && tiedTo != null && !(tiedTo instanceof Player)) {
            event.setCanRender(TriState.FALSE);
        }
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();
        if (entity.level().isClientSide() && entity.tickCount == 1) {
            handleClientFirstTick(entity);
        }
        if (entity.level().isClientSide() && entity instanceof LocalPlayer player) {
            Entity tiedTo = TyingHelper.getTiedTo(player);
            if (tiedTo != null) {
                ModClientPayloadHandlers.handleTiedPlayerCameraEntity(true, player);
            }
        }
        if (entity instanceof RemotePlayer remotePlayer) {
            Entity tiedTo = TyingHelper.getTiedTo(remotePlayer);
            if (tiedTo instanceof Player controller) {
                TyingHelper.restrictTiedPlayerMovement(remotePlayer, controller, false);
            }
        }
    }

    private static void handleClientFirstTick(Entity entity) {
        if (entity instanceof Zuri zuri && zuri.isDancing()) {
            tryPlayDanceTimeFirstTick(zuri);
        }
        if (entity instanceof Rocket rocket && rocket.shouldPlayFuseSound()) {
            tryPlayBurningSound(rocket);
        }
        if (entity instanceof Controllable controllable && controllable.isControllingPlayerValid()) {
            Player controller = controllable.getPlayerController();
            if (Objects.requireNonNull(controller).isLocalPlayer()) {
                Minecraft.getInstance().setCameraEntity(entity);
            }
        }
    }

    private static void tryPlayDanceTimeFirstTick(Zuri zuri) {
        if (!zuri.isSilent()) {
            Minecraft.getInstance().getSoundManager().play(new DanceTimeSoundInstance(zuri));
        }
    }

    private static void tryPlayBurningSound(Rocket rocket) {
        if (!rocket.isSilent()) {
            Minecraft.getInstance().getSoundManager().play(new PrimedRocketSoundInstance(rocket));
        }
    }

    @SubscribeEvent
    public static void onRenderNameTagConditionCheck(RenderNameTagEvent.CanRender event) {
        if (event.getEntity() instanceof InGamePlacedEntity<?>) {
            event.setCanRender(TriState.FALSE);
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

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onMovementInputFirstlyUpdate(MovementInputUpdateEvent event) {
        ((OriginalInputAccessor) event.getInput()).tjChase$setOriginalInput(event.getInput().keyPresses);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onMovementInputUpdate(MovementInputUpdateEvent event) {
        Controllable controlling = Controllable.getControllingMob(event.getEntity());
        if (controlling != null) {
            ControllableMovementHandler.updateControlledMobMovement(controlling, event.getEntity(), event.getInput().keyPresses);
            ClientPacketDistributor.sendToServer(new UpdateControlledEntityPositionPayload(controlling.getSelfAsEntity().getId(), controlling.getSelfAsEntity().position()));
            ClientPacketDistributor.sendToServer(new UpdateInputPayload(controlling.getSelfAsEntity().getId(), event.getInput().keyPresses));
            event.getInput().keyPresses = Input.EMPTY;
            ((ClientInputAccessor) event.getInput()).setMoveVector(Vec2.ZERO);
        }
    }

    private static boolean isPlayerInBlueprint() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            for (BlueprintManager manager : BlueprintManager.getNearbyBlueprints(player.level(), player.getBoundingBox().inflate(BlueprintManager.MAX_LENGTH_ALLOWED))) {
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

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onInteractionKeyMappingFirstlyTriggered(InputEvent.InteractionKeyMappingTriggered event) {
        Player player = Minecraft.getInstance().player;
        if (player != null && Utils.isPassive(player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (ModKeyMappings.KEY_STRUGGLE.matches(event.getKeyEvent())) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null) {
                StruggleInstance struggle = StruggleInstance.getStruggle(player);
                if (struggle != null && struggle.clientStruggle(player)) {
                    ClientPacketDistributor.sendToServer(new StrugglePayload(1));
                }
            }
        }
    }

    @SubscribeEvent
    public static void onMouseFirstlyScroll(InputEvent.MouseScrollingEvent event) {
        Player player = Minecraft.getInstance().player;
        if (player != null && Utils.isPassive(player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onInteractionKeyMappingTriggered(InputEvent.InteractionKeyMappingTriggered event) {
        HitResult hitResult = Minecraft.getInstance().hitResult;
        Player player = Minecraft.getInstance().player;
        if (player != null && event.isPickBlock() && hitResult instanceof EntityHitResult entityHitResult && entityHitResult.getEntity() instanceof InGamePlacedEntity<?> entity) {
            if (event.getHand() == InteractionHand.MAIN_HAND && entity.validItem(player.getItemInHand(event.getHand()))) {
                ClientPacketDistributor.sendToServer(new LoadTemplatePayload(entity.createTemplate()));
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onRenderGuiLayer(RenderGuiLayerEvent.Pre event) {
        Player player = Minecraft.getInstance().player;
        if (player != null && Utils.isPassive(player)) {
            if (ClientPassivePlayerHelper.isGuiLayerDisabled(event.getName())) {
                event.setCanceled(true);
            }
        }
    }
}
