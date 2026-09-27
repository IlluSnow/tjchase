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

package illusnow.tjchase.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import illusnow.tjchase.client.renderer.ModDataTickets;
import illusnow.tjchase.entity.gameplay.TyingHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import software.bernie.geckolib.renderer.base.GeoRenderState;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState & GeoRenderState, M extends EntityModel<? super S>> {
    @Shadow
    protected abstract int getModelTint(S renderState);

    @Shadow
    public abstract Identifier getTextureLocation(S renderState);

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V", at = @At("TAIL"))
    private void extractCustomAlpha(T entity, S reusedState, float partialTick, CallbackInfo ci) {
        if (entity instanceof RemotePlayer player && TyingHelper.getTiedTo(player) == Minecraft.getInstance().getCameraEntity()) {
            reusedState.addGeckolibData(ModDataTickets.CUSTOM_ENTITY_ALPHA, TyingHelper.PLAYER_BEING_TIED_ALPHA);
        }
    }

    @SuppressWarnings("unchecked")
    @WrapOperation(
            method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;IIILnet/minecraft/client/renderer/texture/TextureAtlasSprite;ILnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V"
            ),
            slice = @Slice(
                    from = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;getRenderType(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;ZZZ)Lnet/minecraft/client/renderer/rendertype/RenderType;")
            )
    )
    private void applyCustomAlpha(
            SubmitNodeCollector instance,
            Model<? super S> model,
            Object oRenderState,
            PoseStack poseStack,
            RenderType renderType,
            int packedLight,
            int packedOverlay,
            int tintColor,
            @Nullable TextureAtlasSprite sprite,
            int outlineColor,
            ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay,
            Operation<Void> original) {
        S renderState = (S) oRenderState;
        if (renderState.hasGeckolibData(ModDataTickets.CUSTOM_ENTITY_ALPHA)) {
            int alpha = (int) (renderState.getOrDefaultGeckolibData(ModDataTickets.CUSTOM_ENTITY_ALPHA, 1.0) * 255);
            int color1 = (alpha << 24) + 0xFFFFFF;
            tintColor = ARGB.multiply(color1, getModelTint(renderState));
        }
        if (renderState.getOrDefaultGeckolibData(ModDataTickets.CUSTOM_ENTITY_ALPHA, 1.0) < 1.0) {
            renderType = RenderTypes.entityNoOutline(getTextureLocation(renderState));
        }
        original.call(instance, model, renderState, poseStack, renderType, packedLight, packedOverlay, tintColor, sprite, outlineColor, crumblingOverlay);
    }
}
