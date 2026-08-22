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

import com.google.common.reflect.TypeToken;
import com.mojang.blaze3d.vertex.PoseStack;
import illusnow.tjchase.TJChase;
import illusnow.tjchase.block.ModBlocks;
import illusnow.tjchase.client.model.ModArmPoses;
import illusnow.tjchase.client.network.ModClientPayloadHandlers;
import illusnow.tjchase.client.particle.HarpPlayedNoteParticle;
import illusnow.tjchase.client.particle.TJChaseBuffParticle;
import illusnow.tjchase.client.util.HarpAnimation;
import illusnow.tjchase.item.HarpItem;
import illusnow.tjchase.item.ModItems;
import illusnow.tjchase.network.PlayDanceTimePayload;
import illusnow.tjchase.network.UpdateControlledEntityPayload;
import illusnow.tjchase.particle.ModParticleTypes;
import illusnow.tjchase.tag.ModItemTags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;
import org.jetbrains.annotations.Nullable;

@EventBusSubscriber(modid = TJChase.MODID, value = Dist.CLIENT)
public class ClientModEvents {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {}

    @SubscribeEvent // on the mod event bus only on the physical client
    public static void registerClientPayloadHandlers(RegisterClientPayloadHandlersEvent event) {
        event.register(
                PlayDanceTimePayload.TYPE,
                ModClientPayloadHandlers::handlePlayDanceTime
        );
        event.register(
                UpdateControlledEntityPayload.TYPE,
                ModClientPayloadHandlers::handleUpdateControlledEntity
        );
    }

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

    @SubscribeEvent
    public static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(new IClientItemExtensions() {
            @Nullable
            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity user, InteractionHand hand, ItemStack handItem) {
                if (user.getUsedItemHand() == hand && user.getUseItemRemainingTicks() > 0 && handItem.is(ModItemTags.HARPS)) {
                    return ModArmPoses.HARP_PLAY.getValue();
                }
                return null;
            }

            @Override
            public boolean applyForgeHandTransform(PoseStack poseStack, LocalPlayer player, HumanoidArm arm, ItemStack itemInHand, float partialTick, float equipProcess, float swingProcess) {
                HarpAnimation.applyHarpTransform(poseStack, player, arm, itemInHand, partialTick);
                return false;
            }
        }, ModItems.HARP, ModItems.NETHERITE_HARP);
    }

    @SubscribeEvent
    public static void onRegisterRenderStateModifiers(RegisterRenderStateModifiersEvent event) {
        event.registerEntityModifier(new TypeToken<LivingEntityRenderer<?, ?, ?>>() {}, (entity, state) -> {
            float maxPlayHarpDuration = 0;
            if (entity.isUsingItem() && entity.getUseItem().is(ModItemTags.HARPS)) {
                maxPlayHarpDuration = HarpItem.getPlayDuration(entity.getUseItem(), entity);
            }
            state.setRenderData(ModRenderStateContextKeys.MAX_PLAY_HARP_DURATION, maxPlayHarpDuration);
        });
    }

    @SubscribeEvent
    public static void onRegisterParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticleTypes.TJCHASE_BUFF.get(), TJChaseBuffParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.HARP_PLAYED_NOTE.get(), HarpPlayedNoteParticle.Provider::new);
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        Minecraft mc = Minecraft.getInstance();
        AbstractClientPlayer player = mc.player;
        if (player == null) {
            return;
        }
        ItemStack stack = event.getItemStack();
        ItemInHandRenderer itemInHandRenderer = mc.getEntityRenderDispatcher().getItemInHandRenderer();
        if (stack.is(ModItemTags.HARPS)) {
            if (player.getUseItem() == stack && event.getHand() == player.getUsedItemHand()) {
                HarpAnimation.renderFirstPersonPlayHarpAnimation(event, player, stack, itemInHandRenderer);
            }
        }
        if (event.getHand() == InteractionHand.OFF_HAND
                && event.getItemStack().isEmpty()
                && event.getSwingProgress() > 0
                && event.getSwingProgress() < 0.5
                && player.getItemInHand(InteractionHand.MAIN_HAND).is(ModItemTags.HARPS)) {
            HarpAnimation.renderFirstPersonThrowBlockAnimation(event, player, itemInHandRenderer);
        }
    }
}
