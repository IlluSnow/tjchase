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

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import illusnow.tjchase.item.ModGeoItemShelfFix;
import illusnow.tjchase.util.ItemStackRenderStateMixinHelper;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.ShelfRenderer;
import net.minecraft.client.renderer.blockentity.state.ShelfRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.ShelfBlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ShelfRenderer.class)
public class ShelfRendererMixin {
    @Inject(method = "extractRenderState*", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/item/ItemModelResolver;updateForTopItem(Lnet/minecraft/client/renderer/item/ItemStackRenderState;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/ItemOwner;I)V", shift = At.Shift.AFTER))
    private void extractShelfY(ShelfBlockEntity shelfBlockEntity, ShelfRenderState shelfRenderState, float partialTick, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress, CallbackInfo ci, @Local ItemStack stack, @Local ItemStackRenderState renderState) {
        ((ItemStackRenderStateMixinHelper) renderState).tjChase$setShelfY(ModGeoItemShelfFix.getY(stack.getItem()));
    }

    @Inject(method = "submitItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/item/ItemStackRenderState;submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;III)V"))
    private void addShelfY(ShelfRenderState shelfRenderState, ItemStackRenderState itemStackRenderState, PoseStack poseStack, SubmitNodeCollector nodeCollector, int index, float rotation, CallbackInfo ci) {
        poseStack.translate(0, ((ItemStackRenderStateMixinHelper) itemStackRenderState).tjChase$getShelfY(), 0);
    }
}
