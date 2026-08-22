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

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.blaze3d.vertex.PoseStack;
import illusnow.tjchase.entity.controllable.Controllable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Shadow @Final private Minecraft minecraft;

    @WrapWithCondition(method = "renderItemInHand", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;renderHandsWithItems(FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/player/LocalPlayer;I)V"))
    private boolean disableRenderIfControlling(ItemInHandRenderer instance, float partialTick, PoseStack poseStack, SubmitNodeCollector nodeCollector, LocalPlayer player, int packedLight) {
        return minecraft.player == null || Controllable.getControllingMob(minecraft.player) == null;
    }

    @ModifyExpressionValue(method = "tickFov", at = @At(value = "CONSTANT", args = "floatValue=1.0"))
    private float modifyFOVIfHasControllingMob(float original) {
        LocalPlayer player = minecraft.player;
        Options options = minecraft.options;
        boolean firstPerson = options.getCameraType().isFirstPerson();
        float fovEffectScale = options.fovEffectScale().get().floatValue();
        if (player != null && Controllable.getControllingMob(player) != null) {
            return player.getFieldOfViewModifier(firstPerson, fovEffectScale);
        }
        return original;
    }
}
